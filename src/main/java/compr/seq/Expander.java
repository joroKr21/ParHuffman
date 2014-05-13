package compr.seq;

import java.io.File;

/**
 * @author Georgi Krastev <joro.kr.21@gmail.com>
 */
public interface Expander {

    void expand(File fin, File fout) throws Exception;

    void expand(String fin, String fout) throws Exception;
}
