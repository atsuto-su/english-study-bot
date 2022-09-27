package net.myapp.englishstudybot.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;

import net.myapp.englishstudybot.domain.model.VocabEntity;

/**
 * VocabDaoTest is a test class for VocabDao.class
 * 
 * NOTE:
 * The test for findAll method is omitted because is had already been tested by using Postman
 * with controller and service.
 */
@SpringBootTest
@Transactional
@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class
})
@DbUnitConfiguration(
    dataSetLoader = CsvDataSetLoader.class
)
@DatabaseSetup("/db/data/")
class VocabDaoTest {

    private static final LocalDateTime testCurrentTime 
    = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

    @Autowired
    private VocabDao vocabDao;

    @BeforeAll
    static void setUpAll() {
        MockedStatic<LocalDateTime> mock 
        = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS);
        mock.when(LocalDateTime::now).thenReturn(testCurrentTime);
    }

    @Test
    @DisplayName("指定IDの英単語データを1件取得")
    void findByIdOneVocab() {
        //Arrange
        Integer vocabId = 5;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        VocabEntity vocabExpected
        = new VocabEntity(
            vocabId,
            "refurbish",
            "～を改装する、一新する",
            "refurbish a hotel lounge",
            "ホテルのラウンジを改装する",
            "admin",
            LocalDateTime.parse("2022-09-18 09:05:05", formatter),
            LocalDateTime.parse("2022-09-09 10:01:01", formatter)
        );

        //Act
        VocabEntity vocabActual = vocabDao.findById(vocabId);

        //Assert
        assertThat(vocabActual).usingRecursiveComparison().isEqualTo(vocabExpected);

    }

    @Test
    @DisplayName("存在しないIDの英単語データ取得でnull返却")
    void findByIdNoData() {
        //Arrange
        Integer vocabId = 1000;

        //Act
        VocabEntity vocabActual = vocabDao.findById(vocabId);

        //Assert
        assertThat(vocabActual).isNull();

    }

    @Test
    @DisplayName("ランダムIDの英単語データ取得")
    void findByRandom() {

        //Arrange
        final int REPEAT_COUNT_MAX = 2;

        //Act
        Integer lastVocabId;
        VocabEntity vocabActual = vocabDao.findRandom();
        int i = 0;
        do {
            lastVocabId = vocabActual.getId();
            vocabActual = vocabDao.findRandom();
            i++;
        } while (lastVocabId == vocabActual.getId() || i < REPEAT_COUNT_MAX);
        VocabEntity vocabExpected = vocabDao.findById(vocabActual.getId());

        //Assert 
        // assert that the latest primary id is different from 
        // the previous one and hence record is extracted randomly
        assertThat(lastVocabId != vocabActual.getId()).isTrue();
        // assert that vocabulary record is extracted as expected
        assertThat(vocabActual).usingRecursiveComparison().isEqualTo(vocabExpected);

    }
    
    @Test
    @DisplayName("指定ID以外の英単語データを取得")
    void findSomeExceptForOne() {
        //Arrange
        int findNum = 9;
        Integer vocabIdExcluded = 5;

        //Act
        List<VocabEntity> vocabList = vocabDao.findSomeExceptForOne(findNum, vocabIdExcluded);
        List<Integer> vocabIdUniqueList
         = vocabList.stream().map(item -> item.getId()).distinct().toList();

        VocabEntity vocabActual = vocabList.get(0);
        VocabEntity vocabExpected = vocabDao.findById(vocabActual.getId());

        //Assert
        assertThat(vocabIdUniqueList.size() == findNum).isTrue();
        assertThat(vocabIdUniqueList.contains(vocabIdExcluded)).isFalse();
        assertThat(vocabActual).usingRecursiveComparison().isEqualTo(vocabExpected);

    }

    @Test
    @DisplayName("英単語データを1件新規登録")
    void addOneVocab() {
        //Arrange
        VocabEntity vocabExpected
        = new VocabEntity(
            null,
            "apprentice",
            "弟子、見習い",
            "I’ll help you!! I’m a Holmes’ apprentice!!",
            "ボクが助けてあげるよ！！ボクはホームズの弟子だからさ！！",
            "testUserA",
            testCurrentTime,
            testCurrentTime
        );
        Integer lastId = 10;

        //Act
        VocabEntity vocabActual = vocabDao.add(vocabExpected);
        vocabExpected.setId(vocabActual.getId());

        //Assert
        assertThat(vocabActual).usingRecursiveComparison().isEqualTo(vocabExpected);
        assertThat(vocabActual.getId() > lastId).isTrue();

    }

    @Test
    @DisplayName("英単語データを更新")
    void updateOneVocabSpelling() {
        //Arrange
        Integer vocabId = 7;
        String spelling = "test";
        String meaning = "テスト";
        String exampleEn = "This is a test.";
        String exampleJp = "これはテストです。";
        VocabEntity vocabExpected = vocabDao.findById(vocabId);
        vocabExpected.setSpelling(spelling);
        vocabExpected.setMeaning(meaning);
        vocabExpected.setExampleEn(exampleEn);
        vocabExpected.setExampleJp(exampleJp);
        vocabExpected.setUpdatedAt(testCurrentTime);

        //Act
        VocabEntity vocabActual = vocabDao.update(vocabExpected);

        //Assert
        assertThat(vocabActual).usingRecursiveComparison().isEqualTo(vocabExpected);

    }

    @Test
    @DisplayName("英単語データを1件削除")
    void deleteOneVocab() {
        //Arrange
        Integer vocabId = 1;

        //Act
        VocabEntity vocabBeforeDelete = vocabDao.findById(vocabId);
        vocabDao.delete(vocabId);
        VocabEntity vocabActual = vocabDao.findById(vocabId);

        //Assert
        assertThat(vocabBeforeDelete).isNotNull();
        assertThat(vocabActual).isNull();

    }

}
