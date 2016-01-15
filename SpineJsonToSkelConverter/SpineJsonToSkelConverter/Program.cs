using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SpineJsonToSkelConverter
{
    class Program
    {
        static void Main(string[] args)
        {
            var inputPath = args.Length > 0 ? args[0] : Environment.CurrentDirectory;
            var dirInfo = new DirectoryInfo(inputPath + "/");

#if DEBUG
            dirInfo = new DirectoryInfo(@"D:\work\unity\project\ultraman\client_game\Assets\Common\Spine\uidonghua\kun\kun_dark\");
#endif

            Console.WriteLine(dirInfo.FullName);

            var count = 0;

            var list_json = dirInfo.GetFiles("*.json", SearchOption.AllDirectories);
            foreach (var json in list_json)
            {
                var skel = new FileInfo(json.DirectoryName + "/" + "kun_dark.skel.bytes");
                var skelMeta = new FileInfo(skel.FullName + ".meta");
                if (skel.Exists)
                {
                    if (skelMeta.Exists)
                        skelMeta.Delete();
                    File.Move(json.FullName + ".meta", skelMeta.FullName);
                    json.Delete();
                    count++;
                }
                else
                {
                    Console.WriteLine(skel.FullName + "不存在，跳过");
                }
            }
            Console.WriteLine("处理了" + count + "个文件");
            Console.ReadLine();
        }
    }
}
