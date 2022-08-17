/**************************/
/* スキーマ定義ファイル */
/**************************/
CREATE TABLE IF NOT EXISTS vocabularies (
    id SERIAL PRIMARY KEY UNIQUE,
    spelling VARCHAR(30) NOT NULL CHECK (spelling <> ''),
    meaning VARCHAR(50) NOT NULL CHECK (meaning <> ''),
    example_en VARCHAR(70) NOT NULL,
    example_jp VARCHAR(50) NOT NULL,
    users_id VARCHAR(40) NOT NULL DEFAULT 'admin',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(40) UNIQUE PRIMARY KEY,
    self_word_only BOOLEAN DEFAULT FALSE,
    quiz_status INT NOT NULL DEFAULT 0,
    last_vocabularies_id INT DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS quiz_histories (
    vocabularies_id INT,
    users_id VARCHAR(40),
    total_count_question_en INT DEFAULT 0,
    total_count_question_jp INT DEFAULT 0,
    last_question_date_en TIMESTAMP DEFAULT NULL,
    last_question_date_jp TIMESTAMP DEFAULT NULL,
    total_count_correct_en INT DEFAULT 0,
    total_count_correct_jp INT DEFAULT 0,
    is_last_answer_correct_en BOOLEAN DEFAULT FALSE,
    is_last_answer_correct_jp BOOLEAN DEFAULT FALSE,
    is_quiz_disallowed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (vocabularies_id, users_id)
);

ALTER TABLE vocabularies ADD FOREIGN KEY (users_id) REFERENCES users(id);
ALTER TABLE users ADD FOREIGN KEY (last_vocabularies_id) REFERENCES vocabularies(id);
ALTER TABLE quiz_histories 
    ADD FOREIGN KEY (vocabularies_id) REFERENCES vocabularies(id),
    ADD FOREIGN KEY (users_id) REFERENCES users(id)
;

