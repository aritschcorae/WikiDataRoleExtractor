# WikiDataRoleExtractor
Small application which extracts plays and roles from wikidata and their wikipedia link.
After that  it scraps roles from all wikipedia pages found and cleans the up in memory.
Then a merge is tried to be done on the role names and exported as a CSV file pipe delimted.

User needs to then manually check the out put.

After that, the application creates a file with Quickstatements in it. This is based on the file the user created named 'data-merged.csv' (can be changed in QuickStatementCreator class)


Parameters can be adapted in the default.properties file.
