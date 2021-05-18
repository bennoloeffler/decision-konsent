-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(email, password)
VALUES (:email, :password)

-- :name update-user! :! :n
-- :doc updates an existing user record
UPDATE users
SET password = :password
WHERE email = :email

-- :name get-user :? :1
-- :doc retrieves a user record given the id
SELECT * FROM users
WHERE email = :email

-- :name delete-user! :! :n
-- :doc deletes a user record given the id
DELETE FROM users
WHERE email = :email

-- :name get-users :? :*
-- :doc retrieves all users records
SELECT * FROM users


-- :name create-konsent-no-id! :! :1
-- :doc creates a new konsent record
INSERT INTO konsents
(konsent)
VALUES (:konsent) RETURNING id

-- :name create-konsent! :! :n
-- :doc creates a new konsent record
INSERT INTO konsents
(id, konsent)
VALUES (:id, :konsent)

-- :name update-konsent! :! :n
-- :doc updates an existing konsent record
UPDATE konsents
SET konsent = :konsent
WHERE id = :id

-- :name get-konsent :? :1
-- :doc retrieves a konsent record given the id
SELECT * FROM konsents
WHERE id = :id

-- :name delete-konsent! :! :n
-- :doc deletes a konsent record given the id
DELETE FROM konsents
WHERE id = :id

-- :name get-konsents :? :*
-- :doc retrieves all users records
SELECT * FROM konsents




-- :name create-message! :! :n
-- :doc creates a new message record
INSERT INTO messages
(message)
VALUES (:message)

-- :name get-messages :? :*
-- :doc retrieves all messages records
SELECT * FROM messages
