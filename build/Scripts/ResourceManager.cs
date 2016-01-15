using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using UnityEngine;

public class ResourceManager
{

    static private Dictionary<string, AssetBundle> dic_bundle = new Dictionary<string, AssetBundle>();

    static public GameObject GetResource(string path, string assetName = null)
    {
        path = @"Assets/bundleAssets/" + path;
#if UNITY_EDITORs

        var go = UnityEditor.AssetDatabase.LoadAssetAtPath<GameObject>(path);
        return go;

#elif UNITY_ANDROID || UNITY_EDITOR

#if UNITY_EDITOR
        var curPath = Directory.GetCurrentDirectory();
        var bundlePath = curPath + "/outputBundles/" + path;
#else
        var curPath = Application.persistentDataPath;
        var bundlePath = curPath + "/assetBundles/" + path;
#endif

        Logger.Log("bundlePath", curPath, bundlePath);

        AssetBundle bundle = null;
        if (dic_bundle.TryGetValue(bundlePath, out bundle) == false)
            bundle = AssetBundle.LoadFromFile(bundlePath);

        if (bundle == null)
            return null;
        else
            dic_bundle[bundlePath] = bundle;

        if (string.IsNullOrEmpty(assetName))
        {
            var assetNames = bundle.GetAllAssetNames();
            assetName = assetNames[0];
        }
        var go = bundle.LoadAsset<GameObject>(assetName);
        return go;

#else
        throw new Exception("不支持的平台");
#endif
    }

    static public GameObject GetGameObject(string path, string assetName = null, bool throwExceptionIfNull = true)
    {
        var prefab = GetResource(path);
        if (prefab == null)
        {
            if (throwExceptionIfNull)
                throw new Exception(string.Format("资源读取失败! path:{0},assetName:{1}", path, assetName));
            else
                return null;
        }
        var go = GameObject.Instantiate(prefab) as GameObject;
        return go;
    }

}

