package zad1;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

import static java.nio.file.StandardOpenOption.APPEND;

public class Futil extends SimpleFileVisitor<Path> {

    private static Path resultFName;

    public static void processDir(String dirName, String resultFileName) {

        resultFName = Paths.get(resultFileName);

        File result = new File(resultFileName);
        try {
            if (result.exists()) {
                result.delete();
            }
            result.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Futil futil = new Futil();
        try {
            Files.walkFileTree(Paths.get(dirName), futil);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
        if (file.getFileName().toString().endsWith(".txt")) {
            fileReader(file);
        }
        return FileVisitResult.CONTINUE;
    }

    public FileVisitResult visitFileFailed(Path file, IOException e) {
        System.err.println("Error: visit file is failed.");
        return FileVisitResult.CONTINUE;
    }


    public void fileReader(Path file) throws IOException {

        FileChannel fileInput = FileChannel.open(file);
        FileChannel fileOutput = FileChannel.open(resultFName, EnumSet.of(APPEND));

        //Byte buffer allocation
        ByteBuffer byteBuffer = ByteBuffer.allocate((int) fileInput.size());
        fileInput.read(byteBuffer);

        //Defining input and output encoding
        Charset inCharset = Charset.forName("Cp1250"),
                outCharset = StandardCharsets.UTF_8;

        //Byte buffer decoding
        byteBuffer.flip();
        CharBuffer charBuffer = inCharset.decode(byteBuffer);

        //Character buffer encoding
        byteBuffer = outCharset.encode(charBuffer);

        //Saving to the output file
        fileOutput.write(byteBuffer);

        fileInput.close();
        fileOutput.close();
    }
}
