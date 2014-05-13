# ParHuffman

A Java library for parallel
[Huffman coding](http://en.wikipedia.org/wiki/Huffman_coding)
compression and decompression.

## Usage

### Compression

file: `compr.par.huff.ParHuffCompressor.java`

    usage: java ParHuffCompressor
    options:
     -i,--input <filename>    input file (*required)
     -o,--output <filename>   output file
     -q,--quiet               indicate only progress
     -t,--tasks <number>      number of concurrent tasks

    Compress the input file to the output file
    using Huffman coding with -t tasks.

### Decompression

file: `compr.par.huff.HuffExpander.java`

    usage: java HuffExtractor
    options:
     -i,--input <filename>    input file (*required)
     -o,--output <filename>   output file
     -q,--quiet               indicate only progress

    Extract the Huffman coded input file to the output file.

### Generating frequency table

file: `test.FreqTableTest`

    usage: java FreqTableTest
    options:
     -f,--file <filename>     input file (*required)
     -o,--output <filename>   output file
     -q,--quiet               indicate only progress
     -t,--tasks <number>      number of concurrent tasks

    Build the byte frequency table of the input file
    with -t tasks and save it in the output file.

## License

Copyright © 2014 Georgi Krastev <joro.kr.21@gmail.com>

Distributed under the
[Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html) version 1.
