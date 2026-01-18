import {Component, AfterViewInit, ViewChild, ElementRef, NgZone, Input} from '@angular/core';
import { CommonModule, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Account } from '../model/account';
import { HouseholdMember } from '../model/householdMember';
import { AccountService } from '../service/account.service';
import { HouseholdMemberService } from '../service/householdMember.service';
import { HouseholdService } from '../service/household.service';
import { ExpenditureService } from '../service/expenditure.service';
import { ReceiptCopyService } from '../service/receiptCopy.service';
import { Expenditure } from '../model/expenditure';
import { ReceiptCopy } from '../model/receiptCopy';
import { ExpenditureCategory } from '../model/expenditureCategory';
import { Router } from '@angular/router';

declare var cv: any; // Have to be defined, because the type wasn't allways found ???!

@Component({
  selector: 'app-documentRecognizer',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './documentRecognizer.component.html',
  styleUrls: ['./documentRecognizer.component.css']
})
export class DocumentRecognizerComponent implements AfterViewInit {

  @Input() householdMember!: HouseholdMember;
  @Input() expenditure!: Expenditure;
  title = "Dokument erkennen";
  snapshotUrl: string | null = null;
  @ViewChild('video') video!: ElementRef<HTMLVideoElement>;
  // canvas needed by opencv for transitions
  @ViewChild('canvas') canvas!: ElementRef<HTMLCanvasElement>; // Referenz for opencv.js
  @ViewChild('outputCanvas') outputCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('warpedCanvas') warpedCanvas!: ElementRef<HTMLCanvasElement>;

  // NgZone for asynchronous image analysis (performance reasons)
  constructor(private ngZone: NgZone, private expenditureService: ExpenditureService, private router: Router) {
    this.householdMember = history.state.householdMember;
    this.expenditure = history.state.expenditure;
  }

  orderPoints(points: any[]) {

    // Return Array
    let rect = [null, null, null, null];

    // Sum the Arrayvalues (x+y)
    const sum = points.map(p => p.x + p.y);
    // Spread-Operator spread the Array + return Index of max/min values + Output indexed Array-Content
    rect[0] = points[sum.indexOf(Math.min(...sum))]; // oben-links
    rect[2] = points[sum.indexOf(Math.max(...sum))]; // unten-rechts

    // Difference the Arrayvalues (y-x)
    const diff = points.map(p => p.y - p.x);
        // Spread-Operator spread the Array + return Index of max/min values + Output indexed Array-Content
    rect[1] = points[diff.indexOf(Math.min(...diff))]; // oben-rechts
    rect[3] = points[diff.indexOf(Math.max(...diff))]; // unten-links

    return rect as any[]; //  any[] because of the typing for cv... otherwise it haven't be recognized
  }

  // After Initialisation waiting for load opencv.js and access camera
  async ngAfterViewInit() {
    await this.startCamera();
    this.waitForOpenCV().then(() => this.processFrame()); // Kamerazugriff dauert vielleicht laenger ?!?!? --> asynchron / deswegen noch aktiv 5s warten --> aber eigentlich besser awaiten (mach ich spaeter noch....9
  }

  // get the camera-stream
  async startCamera() {
    const stream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: 'environment' }
    });
    this.video.nativeElement.srcObject = stream;
    // waiting for frames delivered by element
    await new Promise(resolve => {
      this.video.nativeElement.onloadedmetadata = resolve;
    });
  }

  // Wainting until opencv.js is loaded (checking always 2 Secounds)
  waitForOpenCV(): Promise<void> {
    return new Promise(resolve => {
      const check = () => {
        if (cv) {
          resolve();
        } else {
          setTimeout(check, 2000);
        }
      };
      check();
    });
  }

  // Analysed images
  processFrame() {
    // asynchron / running outside Angular
    this.ngZone.runOutsideAngular(() => {
      const video = this.video.nativeElement;
      const canvas = this.canvas.nativeElement;
      const outputCanvas = this.outputCanvas.nativeElement;

      canvas.width = video.videoWidth;
      canvas.height = video.videoHeight;
      outputCanvas.width = video.videoWidth;
      outputCanvas.height = video.videoHeight;

      const process = () => {
        // Frame analysis and transformation
        this.detectDocument();
        // request for run before screen show new frame (to draw canvas smoothly)
        requestAnimationFrame(process);
      };
      // cycle process
      process();
    });
  }

  // Frame analysis and transformation
  detectDocument() {
    const video = this.video.nativeElement;
    const canvas = this.canvas.nativeElement;
    const canvasOut = this.outputCanvas.nativeElement;
    const canvasContext = canvas.getContext('2d')!;
    const canvasContextOutput = canvasOut.getContext('2d')!;

    // frame to canvas
    canvasContext.drawImage(video, 0, 0, video.videoWidth, video.videoHeight);

    // OpenCV Mat's
    let original = cv.imread(canvas);
    let gray = new cv.Mat();
    let blurred = new cv.Mat();
    let thresh = new cv.Mat();
    let edges = new cv.Mat();
    let hierarchy = new cv.Mat();
    let contours = new cv.MatVector();

    // Preprocessing Values from doc/Python_v4
    cv.cvtColor(original, gray, cv.COLOR_RGBA2GRAY);
    cv.GaussianBlur(gray, blurred, new cv.Size(19, 19), 19);
    cv.adaptiveThreshold(blurred, thresh, 255, cv.ADAPTIVE_THRESH_MEAN_C, cv.THRESH_BINARY, 31, 5);
    // Exctract the structur
    cv.Canny(thresh, edges, 50, 150);

    // Find contoures from the image
    cv.findContours(edges, contours, hierarchy, cv.RETR_EXTERNAL, cv.CHAIN_APPROX_SIMPLE);

    // detect only large contours (8%-98% from origin-image)
    const minArea = video.videoWidth * video.videoHeight * 0.08;
    const maxArea = video.videoWidth * video.videoHeight * 0.98;

    // go through contours to find one with 4 corners
    let bestContour = null;
    for (let i = 0; i < contours.size(); i++) {
      const contour = contours.get(i);
      const peri = cv.arcLength(contour, true);
      const approx = new cv.Mat();
      // Contour approximated to fewer points (max 10% deviate from the original perimeter)
      cv.approxPolyDP(contour, approx, 0.1 * peri, true);

      // approximated Contuorspoints  == 4 for Documents
      if (approx.rows === 4) {
        const area = cv.contourArea(approx);
        // detect only large contours (8%-98% from origin-image)
        if (area > minArea && area < maxArea) {
          bestContour = approx;
        }
      }
    }

    // Draw Results
    canvasContextOutput.drawImage(video, 0, 0);
    if (bestContour) {
      //console.log("jaaaaaaa")
      canvasContextOutput.strokeStyle = 'lime';
      canvasContextOutput.lineWidth = 5; // 2 war zu wenig fuer Handy
      canvasContextOutput.beginPath();
      for (let i = 0; i < 4; i++) {
        const p1 = bestContour.intPtr(i, 0)
        if (i === 0)
          canvasContextOutput.moveTo(p1[0], p1[1]);
        else
          canvasContextOutput.lineTo(p1[0], p1[1]);
      }
      canvasContextOutput.closePath();
      canvasContextOutput.stroke();

      // Adjust perspective for secound view
      const data = bestContour.data32S; // flat list of integer
      // Point-Array rom the flat List
      const points = [
        new cv.Point(data[0], data[1]),
        new cv.Point(data[2], data[3]),
        new cv.Point(data[4], data[5]),
        new cv.Point(data[6], data[7])
      ];

      // Order Points
      const rect = this.orderPoints(points);
      const [tl, tr, br, bl] = rect; // result values

      // Witdth top and buttom - calc with help from hypertenuse
      const widthA = Math.hypot(br.x - bl.x, br.y - bl.y);
      const widthB = Math.hypot(tr.x - tl.x, tr.y - tl.y);
      const maxWidth = Math.max(widthA, widthB); // Laengste Distanz als Referenz 100% Breite

      // Hight left and right - calc with help from Hypertenuse
      const heightA = Math.hypot(tr.x - br.x, tr.y - br.y);
      const heightB = Math.hypot(tl.x - bl.x, tl.y - bl.y);
      const maxHeight = Math.max(heightA, heightB); // Laengste Distanz als Referenz 100% Hoehe

      // define destination Points
      const destinationPoints = cv.matFromArray(4, 1, cv.CV_32FC2, [
        0, 0,
        maxWidth - 1, 0,
        maxWidth - 1, maxHeight - 1,
        0, maxHeight - 1
      ]);

      // define Source Points
      const sourcePoints = cv.matFromArray(4, 1, cv.CV_32FC2, [
        tl.x, tl.y,
        tr.x, tr.y,
        br.x, br.y,
        bl.x, bl.y
      ]);

      const M = cv.getPerspectiveTransform(sourcePoints, destinationPoints);
      let warped = new cv.Mat();
      cv.warpPerspective(original, warped, M, new cv.Size(maxWidth, maxHeight));
      cv.imshow(this.warpedCanvas.nativeElement, warped);

      // cleanup
      destinationPoints.delete();
      sourcePoints.delete();
      M.delete();
      warped.delete();
    }

    // Cleanup
    original.delete();
    gray.delete();
    blurred.delete();
    thresh.delete();
    edges.delete();
    hierarchy.delete();
    contours.delete();
    if (bestContour)
      bestContour.delete();
  }

  saveFrame() {
    this.snapshotUrl = this.warpedCanvas.nativeElement.toDataURL('image/png');
  }

  addReceiptCopy(householdMember: HouseholdMember, expenditure: Expenditure) {
    this.expenditureService.setRecognizedDocument(this.snapshotUrl);
    this.router.navigate( ['expenditure-component'], { state: {
        householdMember,
        expenditure
      }
    });
  }
}
