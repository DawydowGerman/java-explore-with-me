CREATE TABLE IF NOT EXISTS hits (
  id             BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  app            VARCHAR(512) NOT NULL,
  uri            VARCHAR(255) NOT NULL,
  ip             VARCHAR(50) NOT NULL,
  timestamp      timestamp(6) without time zone NOT NULL
);