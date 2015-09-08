/* Find file id */
SELECT
	f.id 
FROM
	file_ids f
WHERE 
	f.file_id   = :fileId
