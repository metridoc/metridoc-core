/*
 * Copyright 2010 Trustees of the University of Pennsylvania Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package metridoc.camel.impl.iterator;

import metridoc.camel.iterator.CloseableFileIterator;
import metridoc.camel.iterator.DefaultFileIterator;
import org.apache.camel.Converter;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

/**
 *
 * @author Thomas Barker
 */
@Converter
public class ExcelIteratorCreator extends DefaultFileIterator<HSSFRow> {

    private HSSFSheet sheet;
    private int nextZeroBasedRow = 0;
    private InputStream stream;

    public ExcelIteratorCreator() {}

    public ExcelIteratorCreator(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public boolean hasNext() {
        initializeSheetIfNull();

        return sheet.getRow(nextZeroBasedRow) != null;
    }

    private void initializeSheetIfNull() {

        if (sheet == null) {
            try {
                sheet = new HSSFWorkbook(stream).getSheetAt(0);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public HSSFRow next() {
        initializeSheetIfNull();
        HSSFRow row = sheet.getRow(nextZeroBasedRow);
        
        if (row == null) {
            throw new NoSuchElementException("No more rows left");
        }

        nextZeroBasedRow++;
        return row;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported");
    }

    public static String convertDouble(double number) {

        String result;
        if (Math.floor(number) == number) {
            result = String.valueOf((int) number);
        } else {
            result = String.valueOf(number);
        }

        return result;
    }

    @Converter
    public static String[] convertExcelRow(HSSFRow row) {
        String[] result = new String[row.getLastCellNum()];

        for (int i = 0; i < row.getLastCellNum(); i++) {
            HSSFCell cell = row.getCell(i);
		  if (cell != null) {
	            int type = cell.getCellType();

     	       switch(type) {
     	          case HSSFCell.CELL_TYPE_STRING:
     	               result[i] = cell.getStringCellValue();
     	               break;
	               case HSSFCell.CELL_TYPE_NUMERIC:
     	               result[i] = convertDouble(cell.getNumericCellValue());
     	               break;
     	       }
		  }
        }

        return result;
    }

    public static String[] convertExcelRow(HSSFRow row, int size) {
        String[] result = new String[size];

        for (int i = 0; i < size; i++) {
            HSSFCell cell = row.getCell(i);
		    if (cell != null) {
	            int type = cell.getCellType();

     	        switch(type) {
     	          case HSSFCell.CELL_TYPE_STRING:
     	                result[i] = cell.getStringCellValue();
     	                break;
	               case HSSFCell.CELL_TYPE_NUMERIC:
				        result[i] = convertDouble(cell.getNumericCellValue());
				        break;
     	        }
		    }
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        metridoc.utils.IOUtils.closeQuietly(stream);
    }

    @Override
    public CloseableFileIterator<HSSFRow> doCreate(InputStream inputStream) {
        return new ExcelIteratorCreator(inputStream);
    }
}

