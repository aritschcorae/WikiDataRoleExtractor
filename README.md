# WikiDataRoleExtractor
Small application which extracts plays and roles from wikidata and their wikipedia link.
After that  it scrapes roles from all wikipedia pages found and cleans the up in memory.
Then a merge is tried to be done on the role names and exported as a CSV file pipe delimted.

User needs to then manually check the out put.

After that, the application creates a file with Quickstatements in it. This is based on the file the user created named 'data-merged.csv' (can be changed in QuickStatementCreator class)


Parameters can be adapted in the default.properties file.

#How to
Adjust the default.properties file with the data you want to extract.
Build the project with maven (i.e. mvn clean install)
Start the class DataExtractor. A file named extract_[language]_[playtype].csv is generated (i.e. extract_en_opera.csv)
Manually check the created file. Are the roles matched. Every role which should create a QuickStatement should have a value in the "name" column.
Rename the file to "data-merged.csv" and start the class "QuickStatementCreator". A file named roles_quickstatements.txt is generated. The content can be imported with QuickStatements (https://tools.wmflabs.org/quickstatements/)
