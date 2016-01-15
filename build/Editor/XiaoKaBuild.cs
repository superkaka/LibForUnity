using System;
using System.IO;
using System.Collections.Generic;
using UnityEngine;
using UnityEditor;
using System.Globalization;
using System.Text;
using System.Xml;
using KLib;
using System.Diagnostics;

public class XiaoKaBuild
{

    public const string baseMenu = "XiaoKaBuild/";

    [MenuItem(baseMenu + "BuildAPK_release")]
    static private void buildAPK_release()
    {
        doBuildAPK(false);
    }

    [MenuItem(baseMenu + "BuildAPK_debug")]
    static private void buildAPK_debug()
    {
        doBuildAPK(true);
    }

    [MenuItem(baseMenu + "BuildAPK_debugAndRelease")]
    static private void buildAPK_debugAndRelease()
    {
        doBuildAPK(true);
        doBuildAPK(false);
    }

    private const string APKDir = @"output/android/";
    private const string versionDir = @"Assets\Resources\Data\localData\";
    private const string updateInfoTamplatePath = @"build\tamplate_updateInfo.xml";
    private const string updateInfoPath = APKDir + @"updateInfo.xml";
    private const string changeListPath = @"build\changeList.txt";

    static private void doBuildAPK(bool isDebug)
    {
        var projectPath = Directory.GetCurrentDirectory();
        var buildVer = DateTime.Now.ToString("yyyyMMddHHmm");
        var APKNameEd = "_" + buildVer;
        if (isDebug)
            APKNameEd += "_debug";
        var APKTargetPath = APKDir + "ultraman" + APKNameEd + ".apk";

        var bundleVersion = PlayerSettings.bundleVersion;
        Logger.Log(bundleVersion);
        if (bundleVersion.IndexOf("_") > 0)
            bundleVersion = bundleVersion.Substring(0, bundleVersion.IndexOf("_"));
        Logger.Log(bundleVersion);
        Logger.Log(
            "buildAPK",
            APKTargetPath,
            projectPath + "/key/android.keystore"
           );


        if (!Directory.Exists(APKDir))
            Directory.CreateDirectory(APKDir);
        if (!Directory.Exists(versionDir))
            Directory.CreateDirectory(versionDir);

        File.WriteAllText(versionDir + "version.txt",
            bundleVersion + " (build:" + buildVer + ")");

        var scenes = EditorBuildSettings.scenes;
        var sceneNames = new string[scenes.Length];
        for (int i = 0; i < scenes.Length; i++)
        {
            sceneNames[i] = scenes[i].path;
        }

        if (isDebug)
            PlayerSettings.SetScriptingDefineSymbolsForGroup(BuildTargetGroup.Android, "XiaoKaDebug");
        else
            PlayerSettings.SetScriptingDefineSymbolsForGroup(BuildTargetGroup.Android, "ProductionEnvironment");

        var keystoreName = PlayerSettings.Android.keystoreName;
        PlayerSettings.Android.keystoreName = projectPath + "/key/android.keystore";
        PlayerSettings.Android.keyaliasPass = "mx!@2015";
        PlayerSettings.Android.keystorePass = "mx!@2015";


        PlayerSettings.bundleVersion = bundleVersion + APKNameEd;

        AssetDatabase.Refresh();

        var result = BuildPipeline.BuildPlayer(sceneNames, APKTargetPath, BuildTarget.Android, BuildOptions.None);

        PlayerSettings.Android.keystoreName = keystoreName;
        PlayerSettings.SetScriptingDefineSymbolsForGroup(BuildTargetGroup.Android, "");
        PlayerSettings.bundleVersion = bundleVersion;

        if (!string.IsNullOrEmpty(result))
        {
            throw new Exception("buildAPK failure: " + result);
        }

        var tamplate_updateInfo = File.ReadAllText(updateInfoTamplatePath);
        var APKBytes = File.ReadAllBytes(APKTargetPath);
        tamplate_updateInfo = tamplate_updateInfo.Replace("$(viewVersion)", bundleVersion);
        tamplate_updateInfo = tamplate_updateInfo.Replace("$(buildVersion)", buildVer);
        tamplate_updateInfo = tamplate_updateInfo.Replace("$(md5)", MD5Utils.BytesToMD5(APKBytes));
        tamplate_updateInfo = tamplate_updateInfo.Replace("$(bytesTotal)", APKBytes.Length.ToString());
        tamplate_updateInfo = tamplate_updateInfo.Replace("$(changeList)", File.ReadAllText(changeListPath));

        File.WriteAllText(updateInfoPath, tamplate_updateInfo, Encoding.UTF8);

        Logger.Log("buildAPK complete!", APKTargetPath);

        doAfterBuild();

    }

    [MenuItem(baseMenu + "测试afterBuild.cmd")]
    static private void doAfterBuild()
    {
        EditorUtility.OpenWithDefaultApp(@"build\afterBuild.cmd");
        return;
        var start = new ProcessStartInfo(@"build\afterBuild.cmd");
        start.CreateNoWindow = false;
        start.ErrorDialog = true;
        start.UseShellExecute = true;

        var p = Process.Start(start);
    }

}