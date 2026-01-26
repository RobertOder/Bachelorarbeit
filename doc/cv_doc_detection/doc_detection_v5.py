import cv2
import numpy as np

def order_points(pts):
    pts = pts.reshape(4, 2)
    # Sortiert Punkte nach x
    # pst[:, 0] -> Alle Zeilen und Spalte 0 gibt alle X-WErte
    # tps[(idex),:] --> wende diese Reihenfolge auf alle Punkte an  
    xSorted = pts[np.argsort(pts[:, 0]), :]

    # Linke und rechte Punkte trennen
    leftMost = xSorted[:2, :]   # die 2 links und alle Spalten
    rightMost = xSorted[2:, :]  # die 2 rechts und alle Spalten

    # Falls Raute, dann x werte bei tr und bl gleich, hier die inneren 
    # Werte nach x-sortierung nach y pruefen, notfalls tauschen
    # --> hat fast kleine Relevanz bei float, sonst doch über mittelpunkt und winkel...
    # print(f"Ist {leftMost[1][0]} mit {rightMost[0][0]} gleich")
    if int(leftMost[1][0]) == int(rightMost[0][0]): 
        if leftMost[1][1] > rightMost[0][1]:
            leftMost = xSorted[0, 2]   # die 2 links und alle Spalten
            rightMost = xSorted[1, 3]  # die 2 rechts und alle Spalten
            print("tr.x und bl.x gleich!!! Zuordnung der rechten / linken Gruppe anhand y getauscht")
        else:
            print("war doch richtig. ############## :)")

    # linke Punkte (oben-Links und unten-Links) nach y sortieren
    # pst[:, 1] -> Alle Zeilen und Spalte 1 gibt alle y-Werte
    # tps[(index),:] --> wende diese reihenfolge auf alle (die zwei) Punkte an
    (tl, bl) = leftMost[np.argsort(leftMost[:, 1]), :]

    # rechte Punkte (Oben-Rechts und unten-Rechts) anhand Abstand zu tl bestimmen
    # norm = laenge des vectors = abstand (euklidische distanz)
    # (rightMost - tl, axis=1) --> [dx, dy] = sqrt(dx² + dy²)
    # Abstand von tl -> Punkt1 / Abstand von tl -> Punkt 2 | UND DANN Sortieren
    #D = np.linalg.norm(rightMost - tl, axis=1)
    #(br, tr) = rightMost[np.argsort(D)[::-1]]  # größere Distanz -> br, kleinere -> tr
    (tr, br) = rightMost[np.argsort(rightMost[:, 1]), :]

    return np.array([tl, tr, br, bl], dtype="float32")

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
        #binary = cv2.adaptiveThreshold(blurred, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, blockSize=21, C=5)
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

            widthA = np.linalg.norm(br - bl) # Abstand zwischen unten-rechts und unten-links (untere Kante)
            widthB = np.linalg.norm(tr - tl) # Abstand zwischen oben-rechts und oben-links (obere KAnte)
            maxWidth = int(max(widthA, widthB)) # größere der beiden Breiten / int für pixelgroesse

            heightA = np.linalg.norm(tr - br) # Abstand oben-rechts und unten-rechts (rechte Kante)
            heightB = np.linalg.norm(tl - bl) # Abstand oben-links und unten-links (linke Kante)
            maxHeight = int(max(heightA, heightB)) # größere der beiden Hoehen / int für Pixelgroesse

            # Zielkoordinaten (-1 wegen der Pixelindizes / nicht bei 1 Anfangen)
            dst = np.array([
                [0, 0], # oben-links
                [maxWidth - 1, 0], # oben-rechts
                [maxWidth - 1, maxHeight - 1], # unten-rechts
                [0, maxHeight - 1] # unten-links
            ], dtype="float32")

            # dst beinhaltet Zielkoordinaten, mit neuen Punkten, die das Ziel-Bild definieren
            # rect beinhaltet die zuvor sortierten Punkte
            # Berechnet eine 3×3 Projektionsmatrix M, die einen Punkt (x,y) im Quellbild nach (x',y') im Zielbild abbildet
            M = cv2.getPerspectiveTransform(rect, dst)
            # Originalbild in eine Draufsicht transformieren
            # wendet Matrix M auf das Bild frame an und erzeugt ein neues Bild mit Größe (maxWidth, maxHeight)
            warped = cv2.warpPerspective(frame, M, (maxWidth, maxHeight))
            cv2.imshow("Scanned Document", warped)

        cv2.imshow("Detected document contour", canny_color)
        #cv2.imshow("Detected document contour", original)

        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

    cap.release()
    cv2.destroyAllWindows()

detect_document_edges_from_camera()