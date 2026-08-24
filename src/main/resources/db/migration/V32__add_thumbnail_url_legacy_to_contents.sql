-- contents 테이블에 thumbnail_url_legacy 컬럼 추가
-- R2 마이그레이션 배치 스크립트가 덮어쓰기 전 기존 Cloudinary URL을 백업하는 용도
SET @dbname = DATABASE();
SET @preparedStatement = (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @dbname
       AND TABLE_NAME   = 'contents'
       AND COLUMN_NAME  = 'thumbnail_url_legacy') > 0,
    'SELECT 1',
    'ALTER TABLE contents ADD COLUMN thumbnail_url_legacy VARCHAR(1000) NULL'
  )
);
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
