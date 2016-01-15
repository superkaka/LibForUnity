using System;
using System.Collections.Generic;
using System.Text;

namespace KLib
{
    public class KaKaVerification
    {
        static public bool VerifyPassword(string pwd)
        {
            pwd = pwd.Trim().ToLower();
            return pwd == "xiaoka" || pwd == "小卡";
        }
    }
}
