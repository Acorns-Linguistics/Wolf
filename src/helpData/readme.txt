Creating an empty search view

1) Make sure jhall.jar is in home application project directory.
2) helpFiles needs a dummy web page with at least some text in it
3) Go to the helpData directory (one above those with the web pages)
4) Remove the old JavaHelpSearch directory
4) Type: java -cp ..\..\jhall.jar com.sun.java.help.search.Indexer
5) Type: java -cp ..\..\..\jhall.jar com.sun.java.help.search.QueryEngine ..\JavaHelpSearch from the helpData directory
