package net.myapp.englishstudybot.domain.repository;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.csv.CsvURLDataSet;
import org.springframework.core.io.Resource;

import com.github.springtestdbunit.dataset.AbstractDataSetLoader;

public class CsvDataSetLoader extends AbstractDataSetLoader {
    protected IDataSet createDataSet(Resource resource) throws Exception {
       return new CsvURLDataSet(resource.getURL());
    }
}
