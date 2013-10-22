zlib-violator
=============

This violates all of Java's private variable rules to extend its black hands deep into the innards of hadoop's ZlibDecompressor.

Basically, nothing is sacred anymore.

To test

`mvn package` and `hadoop jar target/zlib-violator-1.0-SNAPSHOT.jar`

And if it prints out a passage from 3 Men in a Boat, consider this a victory (but at what COST!)
