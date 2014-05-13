package compr.par;

import compr.seq.Compressor;
import java.io.File;

/**
 * @author Georgi Krastev <joro.kr.21@gmail.com>
 */
public abstract class ParCompressor implements Compressor {

    private int tasks; // number of tasks

    protected ParCompressor(int tasks) {
        this.tasks = tasks;
    }

    @Override
    public void compress(String fin, String fout) throws Exception {
        compress(new File(fin), new File(fout));
    }

    public int getTasks() {
        return tasks;
    }

    public void setTasks(int tasks) {
        this.tasks = tasks;
    }
}
