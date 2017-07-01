# ir2017

This repository was created to contain all steps of the search engine. For more information about the code, it can be found on **indexer/src/main/java** or **searcher/src/main/java**.

Java and Gradle are required. Follow the commands to index the pages and search for websites:  

To install Gradle:
```
$ sudo add-apt-repository ppa:cwchien/gradle
$ sudo apt-get update

$ sudo apt-get install gradle-ppa

```

To compile the code (it creates jar files in indexer/build/libs/ and searcher/build/libs/):
```
$ gradle clean build
$ gradle clean makeJar

```

To index a collection, create fold index in package and execute as **index** and give the path to the collection (folder must contain only the data):
```
$ cd {repo_path}/search-engine-ir2017
$ mkdir index
$ java -jar indexer/build/libs/indexer-ri-1.0-SNAPSHOT.jar index /path/to/collection/

```

If you have already created the index, along with vocabulary, documents and documents norms, you can send a query to the boolean processor by changing index to **search** as in:
```
$ java -jar indexer/build/libs/indexer-ri-1.0-SNAPSHOT.jar search 'term1 and term2 and term3'

```

If you want to use the search engine with Vector Model, PageRank and Anchor Text, you have to create the necessary files to the search as well:
```
$ java -jar searcher/build/libs/searcher-ri-1.0-SNAPSHOT.jar pagerank alpha /path/to/collection/
```
Aplha is the float value used in the PageRank calculation (ex: 0.8). Please, use the **same path** to the collection you have indexed.

Once you have all necessary files inside the root directory, just execute the server:
```
$ java -jar searcher/build/libs/searcher-ri-1.0-SNAPSHOT.jar search
```

When it tells you that it is fully working. Justo go to **client** directory, open the html file in your browser and have fun.


For more information, read the DOCUMENTATION.
