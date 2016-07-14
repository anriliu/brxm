/*
 *  Copyright 2016-2016 Hippo B.V. (http://www.onehippo.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.onehippo.cms.l10n;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelImportFileReader implements ImportFileReader {

    public ExcelImportFileReader() {
    }

    @Override
    public List<String[]> read(final File file) throws IOException {
        final Workbook workbook;
        try {
            workbook = WorkbookFactory.create(new FileInputStream(file));
        } catch (InvalidFormatException|EncryptedDocumentException e) {
            throw new IOException("Could not read file", e);
        }

        final List<String[]> data = new ArrayList<>();
        final Sheet sheet = workbook.getSheetAt(0);
        final Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            final Row row = rowIterator.next();
            final List<String> rowData = new ArrayList<>();
            for (int c = 0; c < row.getLastCellNum(); c++) {
                Cell cell = row.getCell(c);
                if (cell != null) {
                    rowData.add(cell.getStringCellValue());
                } else {
                    rowData.add("");
                }
            }
            data.add(rowData.toArray(new String[0]));
        }

        return data;
    }

}
