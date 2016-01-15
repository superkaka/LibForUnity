using System;
using System.Collections.Generic;
using System.Text;
using System.Text.RegularExpressions;

namespace KLib
{
    public class ProgramBuildVersion
    {

        public string Source;

        public string ViewVersion;
        public long BuildVersion;

        public int MainVersion;
        public int SecondVersion;
        public int ThridVersion;

        public void Read(string verStr)
        {
            var reg = new Regex(@"(\d+\.\d+\.\d+)\s+\(build:(\d+)\)", RegexOptions.IgnoreCase);

            var match = reg.Match(verStr);
            var matchGroups = match.Groups;
            if (matchGroups.Count >= 3)
            {
                ViewVersion = matchGroups[1].Value;
                BuildVersion = Convert.ToInt64(matchGroups[2].Value);

                var list_ver = ViewVersion.Split(new string[] { "." }, StringSplitOptions.None);
                MainVersion = Convert.ToInt32(list_ver[0]);
                SecondVersion = Convert.ToInt32(list_ver[1]);
                ThridVersion = Convert.ToInt32(list_ver[2]);
            }

            Source = verStr;
        }

        override public string ToString()
        {
            return string.Format(string.Format(
@"ProgramBuildVersion BuildVersion:{0},ViewVersion:{1},MainVersion:{2},SecondVersion:{3},ThridVersion:{4},Source:{5}",
                BuildVersion, ViewVersion, MainVersion, SecondVersion, ThridVersion, Source));
        }

    }
}
