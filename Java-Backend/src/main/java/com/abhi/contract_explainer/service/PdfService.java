package com.abhi.contract_explainer.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
@Service
public class PdfService {

    public String extractText(MultipartFile file) throws IOException  {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {

            // 2. Create a PDFTextStripper, which knows how to pull text out of a PDF
            PDFTextStripper stripper = new PDFTextStripper();

            // 3. Ask the stripper to read all text from the PDF document
            String text = stripper.getText(document);

            // 4. Return the extracted text as a plain Java String
            return text;
        }


    }
}
