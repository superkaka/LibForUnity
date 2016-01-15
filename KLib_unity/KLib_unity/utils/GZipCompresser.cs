using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using Ionic.Zlib;

namespace KLib
{
    public class GZipCompresser
    {

        static public byte[] compress(byte[] bytes)
        {

            var output = new MemoryStream();
            var gzipStream = new GZipStream(output, CompressionMode.Compress, true);
            gzipStream.Write(bytes, 0, bytes.Length);
            gzipStream.Close();
            return output.ToArray();

        }

        static public byte[] uncompress(byte[] bytes)
        {

            byte[] working = new byte[1024 * 20];
            var input = new MemoryStream(bytes);
            var output = new MemoryStream();
            using (Stream decompressor = new GZipStream(input, CompressionMode.Decompress, true))
            {

                int n;
                while ((n = decompressor.Read(working, 0, working.Length)) != 0)
                {
                    output.Write(working, 0, n);
                }

            }
            return output.ToArray();

        }

    }
}
