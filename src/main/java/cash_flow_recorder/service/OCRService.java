package cash_flow_recorder.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.bytedeco.opencv.opencv_core.*;

import org.languagetool.JLanguageTool;
import org.languagetool.language.German;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bytedeco.opencv.global.opencv_core.bitwise_not;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Service
public class OCRService {

    @Value("${tesseract.language}")
    private String language;

    @Value("${tesseract.datapath}")
    private String dataPath;

    @Value("${tesseract.userDefineDpi}")
    private String userDefinedDpi;

    //@Value("${tesseract.charWhiteList}")
    //private String charWhiteList;

    //@Value("${dictionary.file}")
    //private String dictionaryFile;

    @Value("${pattern.sumMatchPattern.regexp}")
    private String sumMatchPattern;

    private static final Logger logger = LoggerFactory.getLogger(OCRService.class);
    private final LevenshteinDistance levenshtein = new LevenshteinDistance();
    private Set<String> dictionary =  new HashSet<>();
    private JLanguageTool langTool = new JLanguageTool(new German());

    /**
     * This method sorts a 2 D point array of type Point2f in order of top-left, top-right, bottom-right, bottom-left
     * @param points - Points of type Pont2f (OpenCV)
     * @return rect - Ordered points of type Point2f (OpenCV - Point as two dimensions (x,y) as 32bit Float)
     */
    private static Point2f[] orderPoints(Point2f[] points) {
        Point2f[] rect = new Point2f[4]; // Define return var

        // Sum and Difference Values for calculate the corners
        double[] sum = new double[4];
        double[] diff = new double[4];
        for (int i = 0; i < 4; i++) {
            sum[i] = points[i].x() + points[i].y();
            diff[i] = points[i].x() - points[i].y();
        }
        // assign the calulated values to the return var
        rect[0] = points[argMin(sum)];  // top-left
        rect[2] = points[argMax(sum)];  // bottom-right
        // changed / different from doc/python_v4 - otherwise mirrored, actualy I dont know why :( ?!
        rect[3] = points[argMin(diff)]; // top-right - (origin index 1)
        rect[1] = points[argMax(diff)]; // bottom-left - (origin index 3)

        return rect;
    }

    /**
     * Method for finding the minimum value in a double-array
     * @param array - Array of type double[]
     * @return index - Index points to minimal value
     */
    private static int argMin(double[] array) {
        int index = 0;
        for(int i = 1; i < array.length; i++) {
            if(array[i] < array[index]){
                index = i;
            }
        }
        return index;
    }

    /**
     * Method for finding the maximum value in a double-array
     * @param array - Array of type double[]
     * @return index- Index points to maximal value
     */
    private static int argMax(double[] array) {
        int index = 0;
        for(int i = 1; i < array.length; i++) {
            if(array[i] > array[index]){
                index = i;
            }
        }
        return index;
    }

    /**
     * Experimental preprocessing to detect document and improove the ocr result
     * @param imagePath - local path to image
     */
    public void preprocess(String imagePath) {
        Mat original = imread(imagePath);
        double imgArea = original.rows() * original.cols();
        Mat grayscale =  new Mat();
        Mat blurred = new Mat();
        Mat binary = new Mat();
        Mat morph = new Mat();
        Mat edges = new Mat();
        Mat kernel = getStructuringElement(MORPH_RECT, new Size(2, 2));
        MatVector contours = new MatVector();
        Mat hierarchy = new Mat();
        Mat warped = new Mat();

        cvtColor(original, grayscale, COLOR_BGR2GRAY); // Convert color image to gray values
        //imwrite(imagePath + "_BGR2GRAY.jpg", grayscale); // write image to check the result
        GaussianBlur(grayscale, blurred, new Size(19, 19), 19); // GaussianBlur better to detect documents, but can destroy thin lines
        //medianBlur(grayscale, blurred, 3); // MedianBlur better for Text, because edges are preserved?
        //imwrite(imagePath + "_GaussianBlur.jpg", blurred);
        // threshold(blurred, binary, 0, 255, THRESH_BINARY | THRESH_OTSU); // bad by dark, shady pictures
        adaptiveThreshold(
                blurred,
                binary,
                255,
                ADAPTIVE_THRESH_MEAN_C, // or ADAPTIVE_THRESH_GAUSSIAN_C?
                THRESH_BINARY,
                31,  // blocksize (try 11–31)
                5   // subtract constant
        );
        //imwrite(imagePath + "_adaptiveThreshold.jpg", binary); // write image to check the result
        //morphologyEx(binary, morph, MORPH_CLOSE, kernel); // better to detect text
        morphologyEx(binary, morph, MORPH_OPEN, kernel); // better to detect documents
        //imwrite(imagePath + "_MORPH_OPEN.jpg", morph); // write image to check the result

        // Erosion again if the edges are to thick
        //binary = morph.clone();
        //erode(binary, binary, kernel);

        // Exctract the structur
        Canny(morph, edges, 50, 150);
        //imwrite(imagePath + "_canny.jpg", edges);

        // Find contoures from the image
        findContours(
                edges,
                contours,
                hierarchy,
                RETR_EXTERNAL,
                CHAIN_APPROX_SIMPLE
        );

        // go through contours to find one with 4 corners
        for (long i = 0; i < contours.size(); i++) {
            Mat contour = contours.get(i);
            contour.convertTo(contour, opencv_core.CV_32FC2); // convert from integer to 32Bit float with 2 channels
            double peri = arcLength(contour, true); // calculate perimeter (true = figure closed)

            // Contour approximated to fewer points (max 10% deviate from the original perimeter)
            Mat approx = new Mat();
            approxPolyDP(contour, approx, 0.1 * peri, true);

            // approximated Contuorspoints  == 4 for Documents
            if (approx.rows() == 4) {
                double area = opencv_imgproc.contourArea(approx); // calculated area from contour
                // detect only large contours (8%-98% from origin-image)
                if (area > 0.08 * imgArea && area < 0.98 * imgArea) {
                    // Convert from Mat tot Point2f
                    Point2f[] pts = new Point2f[4];
                    approx.convertTo(approx, opencv_core.CV_32FC2);
                    FloatIndexer indexer = approx.createIndexer(); // Need Float Indexer to get x / y values from approx
                    for (int j = 0; j < 4; j++) {
                        float x = indexer.get(j, 0, 0);
                        float y = indexer.get(j, 0, 1);
                        pts[j] = new Point2f(x, y); // Convert to type of Point2f
                    }
                    indexer.release(); // release the indexer ressource

                    // Order Points to equalize the image-perspective
                    Point2f[] ordered = orderPoints(pts);
                    Point2f tl = ordered[0];
                    Point2f tr = ordered[1];
                    Point2f br = ordered[2];
                    Point2f bl = ordered[3];

                    // Calculate target witdh and hight via hypertenuse
                    int width = (int) Math.max(
                            Math.hypot(br.x() - bl.x(), br.y() - bl.y()),
                            Math.hypot(tr.x() - tl.x(), tr.y() - tl.y())
                    );
                    int height = (int) Math.max(
                            Math.hypot(tr.x() - br.x(), tr.y() - br.y()),
                            Math.hypot(tl.x() - bl.x(), tl.y() - bl.y())
                    );

                    // Define source and destination Mat() for transformation matrix
                    Mat sourcePoints = new Mat(4, 1, opencv_core.CV_32FC2);
                    FloatIndexer sourceIndex = sourcePoints.createIndexer();
                    sourceIndex.put(0, 0, tl.x(), tl.y()); // top-left
                    sourceIndex.put(1, 0, tr.x(), tr.y()); // top-right
                    sourceIndex.put(2, 0, br.x(), br.y()); // buttom-right
                    sourceIndex.put(3, 0, bl.x(), bl.y()); // buttom-left
                    sourceIndex.release(); // release indexer

                    Mat destinationPoints = new Mat(4, 1, opencv_core.CV_32FC2);
                    FloatIndexer destinationIndex = destinationPoints.createIndexer();
                    destinationIndex.put(0, 0, 0f, 0f); // top-left
                    destinationIndex.put(1, 0, width - 1f, 0f); // top-right
                    destinationIndex.put(2, 0, width - 1f, height - 1f); // bottom-right
                    destinationIndex.put(3, 0, 0f, height - 1f); // bottom-left
                    destinationIndex.release(); // release indexer

                    // Transformation and saving the result
                    Mat M = getPerspectiveTransform(sourcePoints, destinationPoints);
                    warpPerspective(original, warped, M, new Size(width, height));
                    imwrite(imagePath + "_filtered.jpg", warped);
                }
            }
        }

    }

    /**
     * Starts the OCR process and returns the text, items and total amount.
     * @param imagePath - local path to image
     * @return List<String> OCRText, Article, amount
     */
    public List<List<String>> performOcr(String imagePath) {
        Tesseract tesseract = new Tesseract();
        List<List<String>> result = new ArrayList<List<String>>();
        tesseract.setLanguage(language);
        tesseract.setDatapath(dataPath);
        tesseract.setVariable("user_defined_dpi", userDefinedDpi);
        //tesseract.setVariable("tessedit_char_whitelist", charWhiteList);
        try {
            File imageTransformed = new File(imagePath + "_filtered.jpg");
            File imageOriginal = new File(imagePath);
            String ocrResult = null;
            if (imageTransformed.exists()) {
                logger.info("OCR completed for file: " + imagePath + "_filtered.jpg");
                ocrResult = tesseract.doOCR(imageTransformed);
            } else {
                // Hier via VLM (erst, wenn mehr von den anderen Kassenbons erkannt wurde / dauert sonst zu lange) !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                logger.info("OCR completed for file: " + imagePath);
                ocrResult = tesseract.doOCR(imageOriginal);
            }
            //ocrResult = tesseract.doOCR(imageOriginal);
            //String ocrResult =  tesseract.doOCR(new File(imagePath + "_filtered.jpg"));
            //dictionary = loadDictionary(System.getProperty("user.dir") + "/lib/de_DE.dic");
            //dictionary = loadDictionary(System.getProperty("user.dir") + dictionaryFile);
            List<List<String>> ocrDetails = extractDetailsWithRegExPattern(ocrResult);
            //List<List<String>> ocrDetails = extractDetailsWithLLM(ocrResult);
            List<String> ocrText = new ArrayList<>();
            ocrText.add(ocrResult);
            result.add(ocrText);
            result.add(ocrDetails.get(0));
            result.add(ocrDetails.get(1));
            return result;
        } catch (TesseractException e) {
            e.printStackTrace();
            logger.error("OCR failed for file: " + imagePath);
            List<String> errorLine = new ArrayList<>();
            errorLine.add("OCR failed");
            result.add(errorLine);
            return result;
        }
    }

    /**
     * Extract article and amount from the receipt copy via RegExPattern
     * @param ocrResult
     * @return detailsList
     */
    public List<List<String>> extractDetailsWithRegExPattern(String ocrResult){
        // Artikel: Text + Preis (<= 99,99), keine "Summe" etc.
        //Pattern itemPattern = Pattern.compile("(.+?)\\s+\\b(\\d+[,.]\\d{2})\\s.*");
        Pattern amountPattern = Pattern.compile(sumMatchPattern);
        List<List<String>> detailsList = new ArrayList<List<String>>();

        String[] lines = ocrResult.split("\\r?\\n");

        List<String> itemsList = new ArrayList<>();
        double calculatedSum = 0.0;
        double totalPrice = -1.0; // -1 = noch nicht gefunden

        for (String line : lines) {
            // komische Woerter filtern --> erstmal ohne, weil bestimmte Trennzeichen doch wichtig sind
            //String cleanedLine = fixOCRWord(line);
            String cleanedLine = line.replaceAll("\u00A0", " ");
            // Zuerst nach Gesamtbetrag suchen
            Matcher totalMatcher = amountPattern.matcher(cleanedLine.toLowerCase());
            if (totalMatcher.find()) {
                totalPrice = Double.parseDouble(totalMatcher.group(2).replace(",", "."));
                continue;
            }
            // Dann Artikel pruefen
//            Matcher itemMatcher = itemPattern.matcher(cleanedLine);
//            if (itemMatcher.find()) {
//                String item = itemMatcher.group(1).trim();
//                String priceStr = itemMatcher.group(2).replace(",", "."); // Komma → Punkt
//                double price = Double.parseDouble(priceStr);
//
//                // Abgleich mit Worterbuch
//                //String correctedItem = correctOCRText(item);
//                String correctedItem = item; // Funktioneirt noch am Besten, weil Artikel nicht immer klassische Woerter sind
//
//                // Ausschluss bestimmter Worter als Artikel
//                if (correctedItem.toLowerCase().matches(".*(summe|zahlung|rückgeld|steuer|gesamt|total|kartenzahlung|gesamtbetrag|visa|eur|betrag|brutto|netto|%).*")) {
//                    continue;
//                }
//
//                itemsList.add(correctedItem);
//                calculatedSum += price;
//            }
        }

        itemsList = extractArticlesWithLLM(ocrResult);

        // Falls kein expliziter Gesamtbetrag gefunden wurde:
        //double finalTotal = (totalPrice > 0) ? totalPrice : calculatedSum; --> Artikel-Summe nehmen
        //double finalTotal = totalPrice; --> Notfalls einfach den Startwert "-1" nehmen
        double  finalTotal = totalPrice; // Pattern hat gegriffen
        if (totalPrice == -1.0) {
            finalTotal = extractPriceWithLLM(ocrResult); // Pattern hat nicht gegriffen, dann ueber LLM
        }

        // Uebergabe in die Attribute
        String[] Items = itemsList.toArray(new String[0]);
        String ItemsString = itemsList.toString();
        double price = finalTotal;

        // Debug-Ausgabe
        System.out.println("Gefundene Artikel:");
        for (String i : Items) {
            System.out.println(" - " + i);
        }
        // Artikel als ein String in die Entity ReceiptCopy uebernehmen, damit diese in der Datenbank gespeichert werden koennen
        System.out.println("Gefundene Artikel via List.toString:"+ItemsString);
        // Gesamtsumme in die Entität Expenditure uebernehmen
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Gesamtsumme in €: " + price);

        List<String> finalAmount = new ArrayList<>();
        finalAmount.add(String.valueOf(finalTotal));
        detailsList.add(finalAmount);
        detailsList.add(itemsList);
        return detailsList;
    }

    /**
     * Extract amount from the receipt copy via LLM
     * @param ocrResult
     * @return finalTotal
     */
    public List<String> extractArticlesWithLLM(String ocrResult){
        List<String> articles = new ArrayList<>();
        String url = "http://localhost:11434/api/generate";
        if (!ocrResult.isEmpty()) {
            // Vorverarbeitung des ocrResults
            Normalizer.normalize(ocrResult, Normalizer.Form.NFKC);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            Map<String, Object> body = new HashMap<>();
            body.put("model", "qwen3:8b");
            body.put("prompt", "Antworte nur in dem folgenden JSON-Format: (" +
                    " 'article': <articel1, article2> ) Frage: Analysiere den" +
                    " Kassenbons, welcher mit OCR erfasst wurde. Ich möchte die" +
                    " die Artikelnamen 1:1 ohne Preise als flache Liste. Ein Artikel" +
                    " ist immer ein Produkt gefolgt vom Preis. Wenn du dir nicht sicher" +
                    " bist, ob es sich bei diesem Artikel um ein Produkt handelt, lasse" +
                    " ihn weg. Hier das OCR-Ergebnis: [" + ocrResult + "]");
            body.put("format", "json");
            body.put("keep_alive", "0s");
            Map<String, Object> options = new HashMap<>();
            options.put("temperature", 0);
            body.put("options", options);
            body.put("stream", false);
            logger.info("AI-Request-Body sent: " + body);
            HttpEntity<?> entity = new HttpEntity<>(body, headers);
            String posibleArticles;
            try {
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                posibleArticles = response.getBody();
                logger.info("AI-Request-Body receive: " + posibleArticles);
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    JsonNode rootNode = objectMapper.readTree(posibleArticles);
                    JsonNode responseNode = rootNode.path("response");
                    JsonNode innerNode = objectMapper.readTree(responseNode.asText());
                    JsonNode articleNode = innerNode.path("article");
                    articleNode.forEach(node -> articles.add(node.asText()));
                } catch(Exception ex) {
                    logger.error("Failed to parse String to JSON: " + ex.getMessage());
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage());
            }
        }
        return articles;
    }

    /**
     * Extract article from the receipt copy via LLM
     * @param ocrResult
     * @return articleList
     */
    public double extractPriceWithLLM(String ocrResult){
        String posibleSum;
        String finalTotal = "-1.0";
        String url = "http://localhost:11434/api/generate";
        if (!ocrResult.isEmpty()) {
            // Vorverarbeitung des ocrResults
            Normalizer.normalize(ocrResult, Normalizer.Form.NFKC);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            Map<String, Object> body = new HashMap<>();
            body.put("model", "deepseek-r1:8b");
            body.put("prompt", "Antworte nur in dem folgenden JSON-Format: (" +
                    " 'sum': <Betrag als Double> ) Frage: Analysiere den" +
                    " Kassenbons, welcher mit OCR erfasst wurde:" + ocrResult +
                    " Ich möchte das du mir den Gesamtbetrag aus dem OCR-Text raussuchst.");
            body.put("format", "json");
            body.put("keep_alive", "0s");
            Map<String, Object> options = new HashMap<>();
            options.put("temperature", 0);
            body.put("options", options);
            body.put("stream", false);
            logger.info("AI-Request-Body sent: " + body);
            HttpEntity<?> entity = new HttpEntity<>(body, headers);
            try {
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                posibleSum = response.getBody();
                logger.info("AI-Request-Body receive: " + posibleSum);
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    JsonNode rootNode = objectMapper.readTree(posibleSum);
                    JsonNode responseNode = rootNode.path("response");
                    JsonNode innerNode = objectMapper.readTree(responseNode.asText());
                    JsonNode sumNode = innerNode.path("sum");
                    finalTotal = sumNode.asText();
                } catch(Exception ex) {
                    logger.error("Failed to parse String to JSON: " + ex.getMessage());
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage());
            }
        }
        return Double.parseDouble(finalTotal.replace(",", "."));
    }

    /**
     * Extract article and amount from the receipt copy via LLM
     * @param ocrResult
     * @return detailsList
     */
//    public List<List<String>> extractDetailsWithLLM(String ocrResult){ // Alternative zu extractDetailsWithRegExPattern
//        List<List<String>> detailsList = new ArrayList<List<String>>();
//        List<String> itemsList = new ArrayList<>();
//        String posibleSum;
//        String finalTotal = "-1.0";
//        String url = "http://localhost:11434/api/generate";
//        if (!ocrResult.isEmpty()) {
//            // Vorverarbeitung des ocrResults
//            Normalizer.normalize(ocrResult, Normalizer.Form.NFKC);
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Content-Type", "application/json");
//            Map<String, Object> body = new HashMap<>();
//            body.put("model", "deepseek-r1:8b");
//            body.put("prompt", "Antworte nur in dem folgenden JSON-Format: (" +
//                    " 'sum': <Betrag als Double> ) Frage: Analysiere den" +
//                    " Kassenbons, welcher mit OCR erfasst wurde:" + ocrResult +
//                    " Ich möchte das du mir den Gesamtbetrag aus dem OCR-Text raussuchst.");
//            body.put("format", "json");
//            body.put("keep_alive", "0s");
//            Map<String, Object> options = new HashMap<>();
//            options.put("temperature", 0);
//            body.put("options", options);
//            body.put("stream", false);
//            logger.info("AI-Request-Body sent: " + body);
//            HttpEntity<?> entity = new HttpEntity<>(body, headers);
//            try {
//                RestTemplate restTemplate = new RestTemplate();
//                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
//                posibleSum = response.getBody();
//                logger.info("AI-Request-Body receive: " + posibleSum);
//                ObjectMapper objectMapper = new ObjectMapper();
//                try {
//                    JsonNode rootNode = objectMapper.readTree(posibleSum);
//                    JsonNode responseNode = rootNode.path("response");
//                    JsonNode innerNode = objectMapper.readTree(responseNode.asText());
//                    JsonNode sumNode = innerNode.path("sum");
//                    finalTotal = sumNode.asText();
//                } catch(Exception ex) {
//                    logger.error("Failed to parse String to JSON: " + ex.getMessage());
//                }
//            } catch (Exception ex) {
//                logger.error(ex.getMessage());
//            }
//        }
//        List<String> finalAmount = new ArrayList<>();
//        finalAmount.add(finalTotal);
//        detailsList.add(finalAmount);
//        detailsList.add(itemsList);
//        return detailsList;
//    }

//    private String fixOCRWord(String word) {
//        String[] parts = word.split("\\s+");
//        if (parts.length <= 1) return word;
//
//        // Alle Nachbarteile durchprobieren
//        for (int i = 0; i < parts.length - 1; i++) {
//            String joined = parts[i] + parts[i + 1];
//            if (dictionary.contains(joined)) {
//                // Ersetzt die zwei Teile durch das zusammengefuegte Wort
//                StringBuilder sb = new StringBuilder();
//                for (int j = 0; j < parts.length; j++) {
//                    if (j == i) {
//                        sb.append(joined).append(" ");
//                        j++; // den naechsten Teil überspringen
//                    } else {
//                        sb.append(parts[j]).append(" ");
//                    }
//                }
//                return sb.toString().trim();
//            }
//        }
//        return word;
//    }

    //private String correctOCR(String word) {
    //    String bestMatch = word;
    //    int bestDistance = Integer.MAX_VALUE;
    //    int maxDistance = Math.max(2, (int)(word.length() * 0.5)); // 50% der Laenge

    //    for (String item : dictionary) {
    //        item = item.trim();
    //        if (item.length() < 3 || item.equals(word)) continue;

    //        int distance = levenshtein.apply(word.toUpperCase(), item.toUpperCase());
            // Schwellwert 40% des Wortes
    //        if (distance < bestDistance && distance <= maxDistance) {
    //            bestDistance = distance;
    //            bestMatch = item;
    //        }
    //    }
    //    return bestMatch;
    //}

    /**
     * Prüft ein einzelnes Wort und gibt den besten LanguageTool-Vorschlag zurück.
     * Wenn das Wort korrekt ist, wird es unverändert zurückgegeben.
     */
    //public String correctOCR(String word) {
    //    try {
            // LanguageTool prueft das Wort
    //        List<RuleMatch> matches = langTool.check(word);

    //        if (matches.isEmpty()) {
                // Keine Fehler dann Wort ist korrekt
    //            return word;
    //        }

            // LanguageTool liefert Vorschlaege fuer moegliche Korrekturen
    //        for (RuleMatch match : matches) {
    //            List<String> suggestions = match.getSuggestedReplacements();
    //            if (!suggestions.isEmpty()) {
                    // Nimm den ersten Vorschlag (besten Treffer)
    //                return suggestions.get(0);
    //            }
    //        }

    //    } catch (IOException e) {
    //        e.printStackTrace();
    //    }

        // Keine Vorschläge oder Fehler danOriginal zurueckgeben
    //    return word;
    //}

//    public String correctOCRText(String text) {
//        String[] words = text.split("\\s+");
//        StringBuilder corrected = new StringBuilder();
//
//        for (String word : words) {
//            String fixed = correctOCR(word); // hier deine Levenshtein-Methode
//            corrected.append(fixed).append(" ");
//        }
//
//        return corrected.toString().trim();
//    }

//    public String correctOCR(String word) {
//        try {
//            List<RuleMatch> matches = langTool.check(word);
//
//            if (matches.isEmpty()) {
//                return word;
//            }
//
//            String bestSuggestion = word;
//            int bestDistance = Integer.MAX_VALUE;
//
//            for (RuleMatch match : matches) {
//                for (String suggestion : match.getSuggestedReplacements()) {
//                    int distance = levenshteinDistance(word, suggestion);
//                    if (distance < bestDistance) {
//                        bestDistance = distance;
//                        bestSuggestion = suggestion;
//                    }
//                }
//            }
//
//            return bestSuggestion;
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return word;
//    }

    // Standard-Levenshtein-Distanzberechnung / gibt aber auch Bibliotheken... zu spaet gemerkt.... -.-
//    private int levenshteinDistance(String a, String b) {
//        int[][] dp = new int[a.length() + 1][b.length() + 1];
//
//        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
//        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
//
//        for (int i = 1; i <= a.length(); i++) {
//            for (int j = 1; j <= b.length(); j++) {
//                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
//                dp[i][j] = Math.min(Math.min(
//                                dp[i - 1][j] + 1,      // Einfügen
//                                dp[i][j - 1] + 1),     // Löschen
//                        dp[i - 1][j - 1] + cost // Ersetzen
//                );
//            }
//        }
//        return dp[a.length()][b.length()];
//    }

//    private Set<String> loadDictionary(String path) {
//        Set<String> dict = new HashSet<>();
//        List<String> lines = null;
//        try {
//            lines = Files.readAllLines(Paths.get(path));
//        } catch (IOException e) {
//            logger.error(e.getMessage());
//            System.out.println(e.getMessage());
//        }
//        for (String line : lines) {
//            line = line.trim();
//            if (!line.isEmpty() && !line.startsWith("#")) {
//                dict.add(line);
//            }
//        }
//        return dict;
//    }
}
