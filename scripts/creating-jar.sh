path1="../java-solutions/info/kgeorgiy/ja/treshchev/implementor/"
path2="../../java-advanced-2023/modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/"

javac -d . $path2/Impler.java $path2/ImplerException.java $path2/JarImpler.java $path1/Implementor.java

touch MANIFEST.txt
echo "Main-Class: info.kgeorgiy.ja.treshchev.implementor.Implementor" > MANIFEST.txt
echo "" >> MANIFEST.txt

jar cfm Implementor.jar MANIFEST.txt info/*

rm MANIFEST.txt
rm -r info/
