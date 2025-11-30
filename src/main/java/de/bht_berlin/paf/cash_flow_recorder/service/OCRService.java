package de.bht_berlin.paf.cash_flow_recorder.service;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_imgproc.*;

import org.languagetool.JLanguageTool;
import org.languagetool.language.German;
import org.languagetool.rules.RuleMatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @Value("$tesseract.user-defined-dpi")
    private String userDefinedDpi;

    @Value("$(tesseract.char-whitelist)")
    private String charWhiteList;

    @Value("${dictionary.file}")
    private String dictionaryFile;

    private static final Logger logger = LoggerFactory.getLogger(OCRService.class);
    private final LevenshteinDistance levenshtein = new LevenshteinDistance();
    private Set<String> dictionary =  new HashSet<>();
    private JLanguageTool langTool = new JLanguageTool(new German());

    /**
     * Experimental preprocessing to improove the ocr result
     * @param imagePath - local path to image
     */
    public void preprocess(String imagePath){
        // in progress
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
            logger.info("OCR completed for file: " + imagePath);
            String ocrResult = tesseract.doOCR(new File(imagePath));
            //String ocrResult =  tesseract.doOCR(new File(imagePath + "_filtered.jpg"));
            //dictionary = loadDictionary(System.getProperty("user.dir") + "/lib/de_DE.dic");
            dictionary = loadDictionary(System.getProperty("user.dir") + dictionaryFile);
            List<List<String>> ocrDetails = extractDetails(ocrResult);
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
     * Extract article and amount from the receipt copy
     * @param ocrResult
     * @return detailsList
     */
    public List<List<String>> extractDetails(String ocrResult){
        // Artikel: Text + Preis (<= 99,99), keine "Summe" etc.
        Pattern itemPattern = Pattern.compile("(.+?)\\s+(\\d+,\\d{2})\\s*€?");
        Pattern amountPattern = Pattern.compile("(Summe|Gesamt|Total|Kartenzahlung|Gesamtbetrag|VISA|EUR|%)\\s+(\\d+,\\d{2})");
        List<List<String>> detailsList = new ArrayList<List<String>>();

        String[] lines = ocrResult.split("\\r?\\n");

        List<String> itemsList = new ArrayList<>();
        double calculatedSum = 0.0;
        double totalPrice = -1.0; // -1 = noch nicht gefunden

        for (String line : lines) {
            // komische Woerter filtern
            String cleanedLine = fixOCRWord(line);
            // Zuerst nach Gesamtbetrag suchen
            Matcher totalMatcher = amountPattern.matcher(cleanedLine);
            if (totalMatcher.find()) {
                totalPrice = Double.parseDouble(totalMatcher.group(2).replace(",", "."));
                continue;
            }
            // Dann Artikel pruefen
            Matcher itemMatcher = itemPattern.matcher(cleanedLine);
            if (itemMatcher.find()) {
                String item = itemMatcher.group(1).trim();
                String priceStr = itemMatcher.group(2).replace(",", "."); // Komma → Punkt
                double price = Double.parseDouble(priceStr);

                // Abgleich mit Worterbuch
                //String correctedItem = correctOCRText(item);
                String correctedItem = item; // Funktioneirt noch am Besten

                // Ausschluss bestimmter Worter als Artikel
                if (correctedItem.toLowerCase().matches(".*(summe|zahlung|rückgeld|steuer|gesamt|total|kartenzahlung|gesamtbetrag|visa|eur|%).*")) {
                    continue;
                }

                itemsList.add(correctedItem);
                calculatedSum += price;
            }
        }

        // Falls kein expliziter Gesamtbetrag gefunden wurde --> Artikel-Summe nehmen
        double finalTotal = (totalPrice > 0) ? totalPrice : calculatedSum;

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
        System.out.println("Gesamtsumme in €: " + price);

        List<String> finalAmount = new ArrayList<>();
        finalAmount.add(String.valueOf(finalTotal));
        detailsList.add(finalAmount);
        detailsList.add(itemsList);
        return detailsList;
    }

    private String fixOCRWord(String word) {
        String[] parts = word.split("\\s+");
        if (parts.length <= 1) return word;

        // Alle Nachbarteile durchprobieren
        for (int i = 0; i < parts.length - 1; i++) {
            String joined = parts[i] + parts[i + 1];
            if (dictionary.contains(joined)) {
                // Ersetzt die zwei Teile durch das zusammengefuegte Wort
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < parts.length; j++) {
                    if (j == i) {
                        sb.append(joined).append(" ");
                        j++; // den naechsten Teil überspringen
                    } else {
                        sb.append(parts[j]).append(" ");
                    }
                }
                return sb.toString().trim();
            }
        }
        return word;
    }

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

    public String correctOCRText(String text) {
        String[] words = text.split("\\s+");
        StringBuilder corrected = new StringBuilder();

        for (String word : words) {
            String fixed = correctOCR(word); // hier deine Levenshtein-Methode
            corrected.append(fixed).append(" ");
        }

        return corrected.toString().trim();
    }

    public String correctOCR(String word) {
        try {
            List<RuleMatch> matches = langTool.check(word);

            if (matches.isEmpty()) {
                return word;
            }

            String bestSuggestion = word;
            int bestDistance = Integer.MAX_VALUE;

            for (RuleMatch match : matches) {
                for (String suggestion : match.getSuggestedReplacements()) {
                    int distance = levenshteinDistance(word, suggestion);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        bestSuggestion = suggestion;
                    }
                }
            }

            return bestSuggestion;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return word;
    }

    // Standard-Levenshtein-Distanzberechnung / gibt aber auch Bibliotheken... zu spaet gemerkt.... -.-
    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                                dp[i - 1][j] + 1,      // Einfügen
                                dp[i][j - 1] + 1),     // Löschen
                        dp[i - 1][j - 1] + cost // Ersetzen
                );
            }
        }
        return dp[a.length()][b.length()];
    }

    private Set<String> loadDictionary(String path) {
        Set<String> dict = new HashSet<>();
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(path));
        } catch (IOException e) {
            logger.error(e.getMessage());
            System.out.println(e.getMessage());
        }
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
                dict.add(line);
            }
        }
        return dict;
    }
}
