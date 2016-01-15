using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using KLib;
using System.Xml;
using UnityEngine;
using System.Collections;
using System.IO;
using KLib;

namespace Assets.Scripts.update
{

    public class GameUpdater : MonoBehaviour
    {
        private ProgramBuildVersion localVersionInfo = new ProgramBuildVersion();

        private WWW downLoader;

        void Start()
        {
            var verStr = ResUtil.LoadText("Data/localData/version");
            try
            {
                localVersionInfo.Read(verStr);
                Logger.Log(localVersionInfo.ToString());
            }
            catch (Exception e)
            {
                Logger.LogError("本地version读取失败！", verStr);
                Logger.LogException(e);
                return;
            }
        }

        public void checkUpdate()
        {
            var path_updateInfo = "http://172.16.4.158/updateInfo.xml";
            try
            {
                var xml = new XmlDocument();
                xml.Load(path_updateInfo);

                Logger.Log("下载更新配置文件成功", xml.InnerXml);

                var viewVersion = xml.SelectSingleNode("root/viewVersion").InnerText;
                var buildVersion = xml.SelectSingleNode("root/buildVersion").InnerText;
                var updateURL = xml.SelectSingleNode("root/updateURL").InnerText;
                var md5 = xml.SelectSingleNode("root/updateURL").Attributes["md5"].InnerText;
                var changeList = xml.SelectSingleNode("root/changeList").InnerText.Trim();

                Logger.Log("解析配置文件成功", viewVersion, buildVersion, updateURL);
                Logger.Log(changeList);

                if (localVersionInfo.BuildVersion >= Convert.ToInt64(buildVersion))
                {
                    Logger.Log("本地版本为最新，不需要更新");
                    return;
                }

                InvokeRepeating("checkProgress", 0f, 0.3f);

                this.StartCoroutine(doDownLoad(updateURL, md5));

            }
            catch (Exception e)
            {
                Logger.LogError("加载更新配置文件失败", path_updateInfo, e);
            }
        }

        private void checkProgress()
        {
            var progress = 0f;
            if (downLoader != null)
            {
                if (downLoader.isDone)
                    CancelInvoke("checkProgress");

                progress = downLoader.progress;
                Logger.Log("downLoadingAPK", progress);
            }
        }

        private IEnumerator doDownLoad(string url, string md5)
        {
            Logger.Log("startDownLoad", url, md5);
            downLoader = new WWW(url);

            yield return downLoader;

            if (string.IsNullOrEmpty(downLoader.error) == false)
            {
                Logger.LogError("downLoadError", downLoader.url, downLoader.error);
                downLoader = null;
            }
            else
            {
                var bytes = downLoader.bytes;

                var downLoadMD5 = MD5Utils.BytesToMD5(bytes);

                Logger.Log("downLoader Over!", bytes.Length, downLoadMD5);

                downLoader = null;

                if (md5 == downLoadMD5)
                {
                    var targetAPK = Application.persistentDataPath + "/new.apk";

                    File.WriteAllBytes(targetAPK, bytes);

                    Logger.Log("installAPK", targetAPK);
                    CallAndroidJava("QihooInstallAPK", targetAPK);
                }
                else
                {
                    Logger.LogError("md5不一致！", md5, downLoadMD5);
                }

            }

        }

        static public void CallAndroidJava(string methodName, params object[] args)
        {
            Logger.Log("CallAndroidJava", methodName);
            if (Application.isEditor)
                return;
#if UNITY_ANDROID

            GetCurrentActivity().Call(methodName, args);

#endif
        }

#if UNITY_ANDROID
        static public AndroidJavaObject GetCurrentActivity()
        {
            AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
            AndroidJavaObject jo = jc.GetStatic<AndroidJavaObject>("currentActivity");
            return jo;
        }
#endif

    }
}
