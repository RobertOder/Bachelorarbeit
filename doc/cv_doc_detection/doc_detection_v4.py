import cv2
import numpy

def order_points(pts):
    pts = pts.reshape(4, 2)
    # leeres Ergebnis-Arra
    rect = numpy.zeros((4, 2), dtype="float32")
    # Vektor mit 4 Summen, summiert x + y, denn z.B. oben links sind x und y klein
    s = numpy.sum(pts, axis=1) # axis=0 summiert beide Spalten (x-gesamt und y-gesamt) / axis=1 summert pro Zeile (vier mal x+y)
    # Berechnet x - y für jeden Punkt
    # Punkt oben-rechts ist x groß, y klein
    # Für unten-links ist x klein, y groß (negativ)
    diff = numpy.diff(pts, axis=1)
    #print(f'Größe des pts: {len(pts)} with values {pts[0]}, {pts[1]}, {pts[2]}, {pts[3]} und s: {len(s)} und argmin: {numpy.argmin(s)} with value {s[0]}, {s[1]}, {s[2]}, {s[3]}')
    # Standard-OpenCV-Koordinaten (0,0 oben links)
    rect[0] = pts[numpy.argmin(s)]        # oben-links / liefert Index des kleinsten x+y
    rect[2] = pts[numpy.argmax(s)]        # unten-rechts / liefert Index des groessten x+y
    rect[1] = pts[numpy.argmin(diff)]     # oben-rechts / lifert Index des kleinsten x-y
    rect[3] = pts[numpy.argmax(diff)]     # unten-links / liefert Index des groessten x-y (negiativ)

    return rect

def detect_document_edges_from_camera():
    cap = cv2.VideoCapture(1)

    if not cap.isOpened():
        print("Error: Could not open camera.")
        return

    while True:
        ret, frame = cap.read()
        if not ret:
            print("Error: Failed to capture image.")
            break

        original = frame.copy()
        img_area = frame.shape[0] * frame.shape[1]

        grayscale = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        # cv2.imshow('Grayscale', grayscale)

        blurred = cv2.GaussianBlur(grayscale, (5, 5), 0)
        # cv2.imshow('Blurred', blurred)

        #_, binary = cv2.threshold(blurred, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
        binary = cv2.adaptiveThreshold(blurred, 255, cv2.ADAPTIVE_THRESH_MEAN_C, cv2.THRESH_BINARY, blockSize=21, C=5)
        # cv2.imshow('Binary', binary)

        canny = cv2.Canny(binary, 50, 150)
        # cv2.imshow('Canny edge detection', canny)

        contours, _ = cv2.findContours(canny, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        contours = sorted(contours, key=cv2.contourArea, reverse=True)[:5]
        doc_contour = None

        for contour in contours:
            peri = cv2.arcLength(contour, True) # Umfang (True = Geschlossene kontur)
            approx = cv2.approxPolyDP(contour, 0.1 * peri, True) # Kontur auf weniger Punkte approximiert (darf hochstens 10% von originalumfang abweichen)
            if len(approx) == 4:
                area = cv2.contourArea(approx)
                if 0.08 * img_area < area < 0.98 * img_area: # kleine vierecke vermeiden / Flaeche der Kontur muss mindestens 8% und max 98% des Gesamtbildes einnehmen
                    print("Document detected.")
                    doc_contour = approx
                    break
            else:
                print("No document detected.")

        canny_color = cv2.cvtColor(canny, cv2.COLOR_GRAY2BGR) # Fuer den letzten konvertierten Frame

        if doc_contour is not None:
            cv2.drawContours(canny_color, [doc_contour], -1, (0, 255, 0), 2)
            #cv2.drawContours(original, [doc_contour], -1, (0, 255, 0), 2)

            # === PERSPEKTIV-ENTZERRUNG ===
            rect = order_points(doc_contour)

            (tl, tr, br, bl) = rect

            widthA = numpy.linalg.norm(br - bl) # Abstand zwischen unten-rechts und unten-links (untere Kante)
            widthB = numpy.linalg.norm(tr - tl) # Abstand zwischen oben-rechts und oben-links (obere KAnte)
            maxWidth = int(max(widthA, widthB)) # größere der beiden Breiten / int für pixelgroesse

            heightA = numpy.linalg.norm(tr - br) # Abstand oben-rechts und unten-rechts (rechte Kante)
            heightB = numpy.linalg.norm(tl - bl) # Abstand oben-links und unten-links (linke Kante)
            maxHeight = int(max(heightA, heightB)) # größere der beiden Hoehen / int für Pixelgroesse

            # Zielkoordinaten (-1 wegen der Pixelindizes / nicht bei 1 Anfangen)
            dst = numpy.array([
                [0, 0], # oben-links
                [maxWidth - 1, 0], # oben-rechts
                [maxWidth - 1, maxHeight - 1], # unten-rechts
                [0, maxHeight - 1] # unten-links
            ], dtype="float32")

            # Punkte, als neue Grenzwerte des Bildes definieren
            M = cv2.getPerspectiveTransform(rect, dst) # Berechnet eine 3×3 Projektionsmatrix M, die einen Punkt (x,y) im Quellbild nach (x',y') im Zielbild abbildet
            # Originalbild in eine Draufsicht transformieren
            warped = cv2.warpPerspective(frame, M, (maxWidth, maxHeight)) # wendet Matrix M auf das Bild frame an und erzeugt ein neues Bild mit Größe (maxWidth, maxHeight)
            cv2.imshow("Scanned Document", warped)

        cv2.imshow("Detected document contour", canny_color)
        #cv2.imshow("Detected document contour", original)

        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

    cap.release()
    cv2.destroyAllWindows()

detect_document_edges_from_camera()