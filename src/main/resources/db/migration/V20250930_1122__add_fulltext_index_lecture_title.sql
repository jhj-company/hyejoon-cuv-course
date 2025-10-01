ALTER TABLE lectures
    ADD FULLTEXT INDEX ft_idx_lecture_title (lecture_title);