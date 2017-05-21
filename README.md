# ir2017

This repository was created to contain all steps of the search engine. The one created here is the indexer. For more information about the code, it can be found on **indexer/src/main/java**.

Java and Gradle are required. Follow the commands to index the pages and search for websites:  

To install Gradle:
```
$ sudo add-apt-repository ppa:cwchien/gradle
$ sudo apt-get update

$ sudo apt-get install gradle-ppa

```

To compile the code (it creates a jar file in indexer/build/libs/):
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

If you have already created the index, along with vocabulary and documents, you can send a query to the boolean processor by changing index to **search** as in:
```
$ java -jar indexer/build/libs/indexer-ri-1.0-SNAPSHOT.jar search 'term1 and term2 and term3'

```

All index files created will be inside the folder **index** and the vocabulary and documents files will be in the root of the package. Temporary files are sent to /tmp/.


For more information, read the DOCUMENTATION.




