import xml.etree.ElementTree as ET
import os

def get_string_keys(file_path):
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        keys = set()
        for string in root.findall('string'):
            if string.get('translatable') == 'false':
                continue
            keys.add(string.get('name'))
        return keys
    except Exception as e:
        print(f"Error parsing {file_path}: {e}")
        return set()

def main():
    res_path = 'app/src/main/res'
    default_strings = os.path.join(res_path, 'values', 'strings.xml')
    default_keys = get_string_keys(default_strings)
    
    print(f"Default strings: {len(default_keys)} keys found.")
    
    for folder in sorted(os.listdir(res_path)):
        if folder.startswith('values-') and folder != 'values-night' and folder != 'values-v26':
            lang_strings = os.path.join(res_path, folder, 'strings.xml')
            if os.path.exists(lang_strings):
                lang_keys = get_string_keys(lang_strings)
                missing = default_keys - lang_keys
                extra = lang_keys - default_keys
                
                print(f"\nLanguage: {folder}")
                print(f"  Missing keys: {len(missing)}")
                if missing:
                    print(f"  Missing: {sorted(list(missing))}")
                print(f"  Extra keys: {len(extra)}")
                if extra:
                    print(f"  Example extra: {list(extra)[:5]}")

if __name__ == "__main__":
    main()
