DROP TABLE IF EXISTS Birthday;
DROP TABLE IF EXISTS Author;
DROP TABLE IF EXISTS VoiceActor;
DROP TABLE IF EXISTS Studio;
DROP TABLE IF EXISTS Genre;
DROP TABLE IF EXISTS Anime;
DROP TABLE IF EXISTS Movie;
DROP TABLE IF EXISTS SeriesLength;
DROP TABLE IF EXISTS Series;
DROP TABLE IF EXISTS Character;
DROP TABLE IF EXISTS Country;
DROP TABLE IF EXISTS User;
DROP TABLE IF EXISTS Review;
PRAGMA foreign_keys = ON;

CREATE TABLE Birthday (
                          date DATE PRIMARY KEY, age INT NOT NULL);
CREATE TABLE Author (
                        id INT PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        birthday DATE,
                        FOREIGN KEY (birthday) REFERENCES Birthday(date));
CREATE TABLE VoiceActor (
                            id INT PRIMARY KEY,
                            name VARCHAR(255) NOT NULL, birthday DATE,
                            FOREIGN KEY (birthday) REFERENCES Birthday(date));
CREATE TABLE Studio (
                        name VARCHAR(255) PRIMARY KEY, year_of_establishment INT, is_active BOOLEAN);
CREATE TABLE Genre (
                       name VARCHAR PRIMARY KEY, popularity_ranking INT,
                       description VARCHAR(255) NOT NULL);
CREATE TABLE Anime (
                       name VARCHAR(255) PRIMARY KEY,
                       author_id INT NOT NULL,
                       studio VARCHAR(255) NOT NULL,
                       genre VARCHAR(30) NOT NULL,
                       year_aired INT NOT NULL,
                       status TEXT NOT NULL CHECK (status IN ('Aired', 'Airing', 'Unaired')),
                       FOREIGN KEY (author_id) REFERENCES Author(id),
                       FOREIGN KEY (studio) REFERENCES Studio(name),
                       FOREIGN KEY (genre) REFERENCES Genre(name));
CREATE TABLE Movie (
                       anime_name VARCHAR(255) PRIMARY KEY,
                       duration_min INT,
                       FOREIGN KEY (anime_name) REFERENCES Anime(name) ON DELETE CASCADE ON UPDATE CASCADE);
CREATE TABLE SeriesLength (
                              number_of_episodes INT PRIMARY KEY, number_of_cours INT);
CREATE TABLE Series (
                        anime_name VARCHAR(255) PRIMARY KEY, number_of_episodes INT,
                        FOREIGN KEY (number_of_episodes) REFERENCES SeriesLength(number_of_episodes),
                        FOREIGN KEY (anime_name) REFERENCES Anime(name) ON DELETE CASCADE ON UPDATE CASCADE);
CREATE TABLE Character (
                           name VARCHAR(255) NOT NULL,
                           anime_name VARCHAR(255) NOT NULL,
                           va_id INT,
                           height_cm INT,
                           role TEXT NOT NULL CHECK (role IN ('Protagonist', 'Antagonist', 'Supporting', 'Other')), PRIMARY KEY (name, anime_name),
                           FOREIGN KEY (va_id) REFERENCES VoiceActor(id));
CREATE TABLE Country (
                         name VARCHAR(50) PRIMARY KEY,
                         region TEXT NOT NULL CHECK (region IN ('Africa', 'Asia', 'Europe', 'North America', 'Oceania', 'South America')));
CREATE TABLE User (
                      username VARCHAR(255) PRIMARY KEY,
                      country VARCHAR(50),
                      email VARCHAR(255),
                      year_joined INT NOT NULL,
                      FOREIGN KEY (country) REFERENCES Country(name));
CREATE TABLE Review (
                        id INT PRIMARY KEY,
                        user VARCHAR(255) NOT NULL,
                        anime VARCHAR(255) NOT NULL,
                        comment VARCHAR(1000),
                        rating INT NOT NULL,
                        would_recommend BOOLEAN NOT NULL,
                        anime_completed BOOLEAN NOT NULL,
                        FOREIGN KEY (user) REFERENCES User(username),
                        FOREIGN KEY (anime) REFERENCES Anime(name) ON DELETE CASCADE ON UPDATE CASCADE);

INSERT INTO Birthday (date, age) VALUES
                                     ('1973-02-09', 51),
                                     ('1992-06-28', 32),
                                     ('1992-02-26', 32),
                                     ('1980-02-20', 44),
                                     ('1986-12-28', 37),
                                     ('1991-06-26', 33),
                                     ('1988-08-29', 36),
                                     ('1999-02-24', 25),
                                     ('1959-08-23', 65),
                                     ('1968-03-18', 56);
INSERT INTO Author (id, name, birthday) VALUES
                                            (0, 'Makoto Shinkai', '1973-02-09'),
                                            (1, 'Gege Akutami', '1992-02-26'),
                                            (2, 'Sui Ishida', '1986-12-28'),
                                            (3, 'Aka Akasaka', '1988-08-29'),
                                            (4, 'Hiroshi Onogi', '1959-08-23');
INSERT INTO Author (id, name) VALUES
                                  (5, 'Hiromu Arakawa'),
                                  (6, 'Haruichi Furudate');
INSERT INTO VoiceActor (id, name, birthday) VALUES
                                                (0, 'Yuichi Nakamura', '1980-02-20'),
                                                (1, 'Natsuki Hanae', '1991-06-26'),
                                                (2, 'Konomi Kohara', '1992-06-28'),
                                                (3, 'Yurie Igoma', '1999-02-24'),
                                                (4, 'Shinichiro Miki', '1968-03-18');
INSERT INTO Studio (name, year_of_establishment, is_active) VALUES
                                                                ('CoMix Wave Films', 2007, true),
                                                                ('MAPPA', 2011, true),
                                                                ('Pierrot', 1979, true),
                                                                ('A-1 Pictures', 2005, true),
                                                                ('Doga Kobo', 1973, true),
                                                                ('Bones', 1998, true),
                                                                ('Production I.G', 1987, true);
INSERT INTO Genre (name, popularity_ranking, description) VALUES
                                                              ('Drama', 1, 'Plot-driven stories focused on realistic characters experiencing human struggle.'),
                                                              ('Adventure', 2, 'Embark on thrilling journeys filled with excitement, danger, and discovery.'),
                                                              ('Dark Fantasy', 3, 'Fantasy with gloomy, dark tone or a sense of horror and dread.'),
                                                              ('Comedy', 4, 'Taking viewers to a better mood and serve as an entertainment.'),
                                                              ('Sports', 5, 'Training for and participating in a sport takes priority, with the goal of furthering athletic abilities to win a competition or achieve some social standing.');
INSERT INTO Anime (name, author_id, studio, genre, year_aired, status) VALUES
                                                                           ('Kimi no Na wa.', 0, 'CoMix Wave Films', 'Drama', 2016, 'Aired'),
                                                                           ('Tenki no Ko', 0, 'CoMix Wave Films', 'Drama', 2019, 'Aired'),
                                                                           ('Jujutsu Kaisen Season 1', 1, 'MAPPA', 'Adventure', 2020, 'Aired'),
                                                                           ('Jujutsu Kaisen 0 Movie', 1, 'MAPPA', 'Adventure', 2021, 'Aired'),
                                                                           ('Tokyo Ghoul: RE', 2, 'Pierrot', 'Dark Fantasy', 2018, 'Aired'),
                                                                           ('Kaguya-sama: Love is War Season 1', 3, 'A-1 Pictures', 'Comedy', 2019, 'Aired'),
                                                                           ('Kaguya-sama wa Kokurasetai: First Kiss wa Owaranai', 3, 'A-1 Pictures', 'Comedy', 2022, 'Aired'),
                                                                           ('Oshi No Ko Season 1', 4, 'Doga Kobo', 'Drama', 2023, 'Aired'),
                                                                           ('Fullmetal Alchemist: Brotherhood', 5, 'Bones', 'Dark Fantasy', 2009, 'Aired'),
                                                                           ('Haikyuu!! Movie: Gomisuteba no Kessen', 6, 'Production I.G', 'Sports', 2024, 'Aired');
INSERT INTO Movie (anime_name, duration_min) VALUES
                                                 ('Kimi no Na wa.', 104),
                                                 ('Tenki no Ko', 112),
                                                 ('Kaguya-sama wa Kokurasetai: First Kiss wa Owaranai', 96),
                                                 ('Jujutsu Kaisen 0 Movie', 85),
                                                 ('Haikyuu!! Movie: Gomisuteba no Kessen', 104);
INSERT INTO SeriesLength (number_of_episodes, number_of_cours) VALUES
                                                                   (24, 2),
                                                                   (12, 1),
                                                                   (11, 1),
                                                                   (64, 5),
                                                                   (13, 1);
INSERT INTO Series (anime_name, number_of_episodes) VALUES
                                                        ('Jujutsu Kaisen Season 1', 24),
                                                        ('Tokyo Ghoul: RE',13),
                                                        ('Kaguya-sama: Love is War Season 1', 12),
                                                        ('Oshi No Ko Season 1', 11),
                                                        ('Fullmetal Alchemist: Brotherhood', 64);
INSERT INTO Character (name, anime_name, va_id, height_cm, role) VALUES
                                                                     ('Satoru Gojo', 'Jujutsu Kaisen Season 1', 0, 190, 'Protagonist'),
                                                                     ('Ken Kaneki', 'Tokyo Ghoul: RE', 1, 170, 'Protagonist'),
                                                                     ('Chika Fujiwara', 'Kaguya-sama: Love is War Season 1', 2, 154, 'Protagonist'),
                                                                     ('Ruby Hoshino', 'Oshi No Ko Season 1', 3, 158, 'Protagonist'),
                                                                     ('Roy Mustang', 'Fullmetal Alchemist: Brotherhood', 4, 180, 'Protagonist');
INSERT INTO Country (name, region) VALUES
                                       ('Hong Kong', 'Asia'),
                                       ('Canada', 'North America'),
                                       ('China', 'Asia'),
                                       ('Australia', 'Oceania'),
                                       ('Dubai', 'Asia');
INSERT INTO User (username, country, email, year_joined) VALUES
                                                             ('ryanttchan', 'Hong Kong', 'ryan@gmail.com', 2020),
                                                             ('animegod', 'Canada', 'animegod@gmail.com', 2018),
                                                             ('animelord', 'China', 'animelord@gmail.com', 2019),
                                                             ('anonymous', 'Australia', 'anonymous@gmail.com', 2023),
                                                             ('randomguy', 'Dubai', 'random@gmail.com', 2021);
INSERT INTO Review (id, user, anime, comment, rating, would_recommend, anime_completed) VALUES

                                                                                            (0, 'ryanttchan', 'Jujutsu Kaisen Season 1', 'Anime of the year', 10, TRUE, TRUE),
                                                                                            (1, 'animegod', 'Tokyo Ghoul: RE', 'Worst anime that I have watched', 0, FALSE, FALSE),
                                                                                            (2, 'animelord', 'Kaguya-sama: Love is War Season 1', 'Overrated anime', 7, FALSE, TRUE),
                                                                                            (3, 'anonymous', 'Oshi No Ko Season 1', 'Worth the hype', 9, TRUE, TRUE),
                                                                                            (4, 'randomguy', 'Fullmetal Alchemist: Brotherhood', 'Best anime series of all time', 10, TRUE, TRUE),
                                                                                            (5, 'ryanttchan', 'Tenki no Ko', 'Anime of the year', 10, TRUE, TRUE),
                                                                                            (6, 'animegod', 'Tenki no Ko', 'Worst anime that I have watched', 0, FALSE, FALSE),
                                                                                            (7, 'animelord', 'Tenki no Ko', 'Overrated anime', 7, FALSE, TRUE),
                                                                                            (8, 'anonymous', 'Tenki no Ko', 'Worth the hype', 9, TRUE, TRUE),
                                                                                            (9, 'randomguy', 'Tenki no Ko', 'Best anime series of all time', 10, TRUE, TRUE);