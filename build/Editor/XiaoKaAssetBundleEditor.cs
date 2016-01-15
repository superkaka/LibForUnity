using UnityEngine;
using UnityEditor;
using System.IO;
using System.Collections.Generic;
using System.Text;
using System.Diagnostics;
using System;

public class XiaoKaAssetBundleEditor
{

    public const string baseMenu = "资源打包/";

    public const string bundlePath = "DLC/";

    [MenuItem(baseMenu + "打包测试大小")]
    static private void doPackageTest()
    {
        var buildTarget = BuildTarget.Android;
        var outputPath = "J:/testBundle/";

        if (Directory.Exists(outputPath) == false)
            Directory.CreateDirectory(outputPath);
        var manifest = BuildPipeline.BuildAssetBundles(outputPath, BuildAssetBundleOptions.None, buildTarget);
        Logger.Log("资源测试打包完成");
    }

    [MenuItem(baseMenu + "打包(windows) ——编辑器测试用")]
    static private void doPackageWindows()
    {
        doPackage(BuildTarget.StandaloneWindows64, bundlePath + "windows/");
    }

    [MenuItem(baseMenu + "打包(android)")]
    static private void doPackagAndroid()
    {
        doPackage(BuildTarget.Android, bundlePath + "android/");
    }

    static private void doPackage(BuildTarget buildTarget, string outputPath)
    {
        Logger.Log("资源打包", buildTarget, outputPath);
        //if (Directory.Exists(bundlePath))
        //    Directory.Delete(bundlePath, true);
        if (Directory.Exists(outputPath) == false)
            Directory.CreateDirectory(outputPath);
        var manifest = BuildPipeline.BuildAssetBundles(outputPath, BuildAssetBundleOptions.UncompressedAssetBundle, buildTarget);

        var bundleNames = manifest.GetAllAssetBundles();
        //bundleNames = manifest.GetAllAssetBundlesWithVariant();
        Logger.Log(bundleNames);


        var dic_nameToBundle = new Dictionary<string, string>();
        foreach (var bundleName in bundleNames)
        {
            var bundle = AssetBundle.LoadFromFile(outputPath + bundleName);
            var assetNames = bundle.GetAllAssetNames();
            bundle.Unload(false);
            foreach (var assetName in assetNames)
            {
                dic_nameToBundle[assetName] = bundleName;
            }
        }

        var sb = new StringBuilder();
        foreach (var key in dic_nameToBundle.Keys)
        {
            sb.Append(key + "=" + dic_nameToBundle[key]);
            sb.Append("\r\n");
        }

        File.WriteAllText(outputPath + "assetNameMap.txt", sb.ToString(), Encoding.UTF8);

        Logger.Log("生成bundle成功", outputPath);

        var DLCReleasePath = "none";
        switch (buildTarget)
        {
            case BuildTarget.Android:
                DLCReleasePath = "android";
                break;
            case BuildTarget.StandaloneWindows64:
                DLCReleasePath = "windows";
                break;
            case BuildTarget.iOS:
                DLCReleasePath = "ios";
                break;
        }

        DLCReleasePath = "output/" + DLCReleasePath + "/assetBundle";

        var start = new ProcessStartInfo(@"build\buildAssetBundle.cmd");
        start.CreateNoWindow = false;
        start.ErrorDialog = true;
        start.UseShellExecute = true;
        start.Arguments = outputPath + " " + DLCReleasePath;
        var p = Process.Start(start);
        p.WaitForExit();

        Logger.Log("复制初始资源包...");
        /*
        DLCReleasePath += "/" + File.ReadAllText(DLCReleasePath + "/assetVersion.txt") + "/";

        var StreamingAssetsDir = "Assets/StreamingAssets/assetBundle/";
        if (Directory.Exists(StreamingAssetsDir))
            Directory.Delete(StreamingAssetsDir, true);

        var StreamingAssetsList = File.ReadAllText("build/StreamingAssetsList.txt");
        var list_StreamingAssets = StreamingAssetsList.Split(new string[] {@"
"}, StringSplitOptions.RemoveEmptyEntries);
        */
        var StreamingAssetsDir = "Assets/StreamingAssets/assetBundle/";
        var list_StreamingAssets = new List<string>();
        foreach (var bundleName in bundleNames)
        {
            var assetImporter = AssetImporter.GetAtPath(bundleName);
            if (assetImporter == null)
                continue;
            if (assetImporter.GetUserData().isFirstBundle)
                list_StreamingAssets.Add(bundleName);
        }

        foreach (var item in list_StreamingAssets)
        {
            var itemPath = item.Trim();
            Logger.Log("复制" + itemPath);
            var startName = DLCReleasePath + itemPath;
            var destName = StreamingAssetsDir + itemPath;
            if (Directory.Exists(startName))
            {
                Logger.Log(itemPath + "为目录，全部复制...");
                CopyDirectory(startName, destName);
                continue;
            }
            var dir = Path.GetDirectoryName(destName);
            if (Directory.Exists(dir) == false)
                Directory.CreateDirectory(dir);
            File.Copy(startName, destName);
        }

        Logger.Log("复制初始资源包完成.");

    }


    [MenuItem(baseMenu + "测试加载bundle")]
    static private void doTestLoad()
    {
        Logger.Log("TestBundleLoad");
        var bundle = AssetBundle.LoadFromFile(bundlePath + @"assets/bundleassets/gateselect/stages/2/stageselect.prefab");
        var assetNames = bundle.GetAllAssetNames();
        Logger.Log(bundle);
        bundle.Unload(false);
        Logger.Log(bundle);
    }

    [MenuItem(baseMenu + "将路径设置为资源包名")]
    [MenuItem("Assets/将路径设置为资源包名", false, -1)]
    static private void AutoSetAssetBundleName()
    {
        var assets = Selection.GetFiltered(typeof(UnityEngine.Object), SelectionMode.Assets);
        foreach (var asset in assets)
        {
            var path = AssetDatabase.GetAssetPath(asset.GetInstanceID());
            path = path.ToLower();
            var assetImporter = AssetImporter.GetAtPath(path);
            assetImporter.assetBundleName = path;
            assetImporter.assetBundleVariant = null;
            Logger.Log("设置了资源包名", asset.name, path);
        }
        AssetDatabase.Refresh();
    }

    [MenuItem(baseMenu + "清除资源包名(包括子项)")]
    [MenuItem("Assets/清除资源包名(包括子项)", false, 1)]
    static private void ClearAssetBundleNames()
    {
        var assets = Selection.GetFiltered(typeof(UnityEngine.Object), SelectionMode.DeepAssets);
        foreach (var asset in assets)
        {
            var path = AssetDatabase.GetAssetPath(asset.GetInstanceID());
            path = path.ToLower();
            var assetImporter = AssetImporter.GetAtPath(path);
            if (string.IsNullOrEmpty(assetImporter.assetBundleName))
                continue;
            assetImporter.assetBundleName = null;
            //assetImporter.assetBundleVariant = null;
            Logger.Log("清除了资源包名", asset.name, path);
        }
        AssetDatabase.Refresh();
    }

    static void CopyDirectory(string srcDir, string tgtDir)
    {
        DirectoryInfo source = new DirectoryInfo(srcDir);
        DirectoryInfo target = new DirectoryInfo(tgtDir);

        if (target.FullName.StartsWith(source.FullName, StringComparison.CurrentCultureIgnoreCase))
        {
            throw new Exception("父目录不能拷贝到子目录！");
        }

        if (!source.Exists)
        {
            return;
        }

        if (!target.Exists)
        {
            target.Create();
        }

        FileInfo[] files = source.GetFiles();

        for (int i = 0; i < files.Length; i++)
        {
            File.Copy(files[i].FullName, target.FullName + @"\" + files[i].Name, true);
        }

        DirectoryInfo[] dirs = source.GetDirectories();

        for (int j = 0; j < dirs.Length; j++)
        {
            CopyDirectory(dirs[j].FullName, target.FullName + @"\" + dirs[j].Name);
        }
    }

}
