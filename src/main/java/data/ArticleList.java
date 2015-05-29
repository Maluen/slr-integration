package data;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

public class ArticleList extends ArrayList<Article> {

	private static final long serialVersionUID = -571659348261704116L;

	public void saveAsCSV(String path) throws IOException {
		
        final CellProcessor[] processors = new CellProcessor[] { 
        		new Optional(), // source
        		new Optional(), // id
        		new Optional(), // title
        		new Optional(), // abstract
        		new Optional(), // keywords
        		new Optional(), // year
        		new Optional(), // authors
        		new Optional(), // publication
        };
        
        ICsvBeanWriter beanWriter = null;
        try {
            beanWriter = new CsvBeanWriter(new FileWriter(path), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
            
            // the header elements are used to map the bean values to each column (names must match)
            final String[] header = new String[] { "source", "id", "title", "abstract",
                    							   "keywords", "year", "authors", "publication" };

            // write the header
            beanWriter.writeHeader(header);
            
            // write the beans
            for( final Article article : this ) {
            	beanWriter.write(article, header, processors);
            }
        	
        } finally {
            if( beanWriter != null ) {
                beanWriter.close();
            }
        }
		
	}
	
}
