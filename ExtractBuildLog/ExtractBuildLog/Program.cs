using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtractBuildLog
{
    class Program
    {
        static void Main(string[] args)
        {
            try
            {
                var logPath = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData) +
                              @"\Unity\Editor\Editor.log";

                var outputPath = args.Length > 0 ? args[0] : Environment.CurrentDirectory;
                var dirInfo = new DirectoryInfo(outputPath + "/");
                var nowTime = DateTime.Now;
                outputPath = dirInfo.FullName + nowTime.ToString("MM-dd HHmm") + ".log";

                Console.WriteLine("logPath:     " + logPath);
                Console.WriteLine("outputPath:  " + outputPath);

                var start = @"Used Assets and files from the Resources folder, sorted by uncompressed size:";
                var end = @"AndroidSDKTools:";

                var log = File.ReadAllText(logPath);
#if DEBUG
                var extract = log;
#else
                var extract = log.Substring(log.IndexOf(start), log.IndexOf(end));
#endif
                File.WriteAllText(outputPath, extract);
                Console.WriteLine("buildLog提取成功");
            }
            catch (Exception e)
            {
                Console.WriteLine("解析Editor.log失败");
                Console.WriteLine(e.Message);
                Console.ReadLine();
            }
        }
    }
}
