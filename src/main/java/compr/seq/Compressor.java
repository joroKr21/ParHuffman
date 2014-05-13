package compr.seq;

import java.io.File;

/**
 * @author Georgi Krastev <joro.kr.21@gmail.com>
 */
public interface Compressor {

    void compress(File fin, File fout) throws Exception;

    void compress(String fin, String fout) throws Exception;
}
