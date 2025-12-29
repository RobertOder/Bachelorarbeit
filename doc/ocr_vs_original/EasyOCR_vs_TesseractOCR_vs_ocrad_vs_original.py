from easyocr import Reader
import pytesseract
# from paddleocr import PaddleOCR --> nicht installiert bekommen, ohne von python downzugraden
# import keras_ocr --> nicht installiert bekommen, ohne von python downzugraden
import subprocess


with open("original.txt", "r", encoding="utf-8") as f:
    original = f.read().splitlines()
originalText = []
for line in original:
    originalText.extend([word.upper() for word in line.split()])

easyReader = Reader(['de'])
easyResult = easyReader.readtext("1765130668IMG_20251206_204234.jpg_filtered.jpg")
easyText = []
for (bbox, text, prob) in easyResult:
    easyText.extend(text.split())

tesseractResult = pytesseract.image_to_string("1765130668IMG_20251206_204234.jpg_filtered.jpg", lang="deu")
tessteractLines = tesseractResult.splitlines()
tesseractText = []
for line in tessteractLines:
    tesseractText.extend(line.split())

#paddleOcr = PaddleOCR(use_angle_cls=True, lang='de')
#paddleResult = paddleOcr.ocr("1765130668IMG_20251206_204234.jpg_filtered.jpg", cls=True)
#paddleText = []
#for block in paddleResult:
#    for line in block:
#        text = line[1][0]
#        paddleText.extend(text.split())

#pipeline = keras_ocr.pipeline.Pipeline()
#kerasImage = keras_ocr.tools.read("1765130668IMG_20251206_204234.jpg_filtered.jpg")
#prediction_groups = pipeline.recognize(kerasImage)
#keraText = [w for w, b in prediction_groups[0]]

ocradWords = []
try:
    # Ocrad aufrufen - jedoch nur PNG?!
    ocradResult = subprocess.run(
        ["ocrad", "--format=utf8", "/tmp/1765130668IMG_20251206_204234.jpg_filtered.png"],
        capture_output=True,
        text=True,
        check=True
    )
    ocradText = ocradResult.stdout

    # Text in Woerter splitten
    ocradWords = ocradText.split()
except subprocess.CalledProcessError as e:
    print(f"Fehler bei OCR: {e}")

oriNumber = len(originalText)
easyCorrect = [w for w in easyText if w.upper() in originalText]
tesseractCorrect = [w for w in tesseractText if w.upper() in originalText]
#paddleCorrect = [w for w in paddleText if w.upper() in originalText]
#keraCorrect = [w for w in keraText if w.upper() in originalText]
ocradCorrect = [w for w in ocradWords if w.upper() in originalText]

print(f"EasyOCR-Uebereinstimmungen: {easyCorrect}\n\n\n")
print(f"TesseractOCR-Uebereinstimmungen: {tesseractCorrect}\n\n\n")
#print(f"PaddleOCR-Uebereinstimmungen: {paddleCorrect}\n\n\n")
#print(f"KeraOCR-Uebereinstimmungen: {keraCorrect}\n\n\n")
print(f"OcradOCR-Uebereinstimmungen: {ocradCorrect}\n\n\n")
print(f"Erkennte Woerter von EasyOCR: {len(easyCorrect)}/{oriNumber}")
print(f"Erkennte Woerter von TesseractOCR: {len(tesseractCorrect)}/{oriNumber}")
#print(f"Erkennte Woerter von KeraOCR: {len(keraCorrect)}/{oriNumber}")
#print(f"Erkennte Woerter von PaddleOCR: {len(paddleCorrect)}/{oriNumber}")
print(f"Erkennte Woerter von OcradOCR: {len(ocradCorrect)}/{oriNumber}")
