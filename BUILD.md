## Steps to build PDFium library for Android

### Prepare a clean ubuntu system and install required dependencies:

```
apt-get update
apt-get install -y  build-essential git subversion pkg-config python libtool cmake glib2.0-dev libatspi2.0-dev wget
```

### Prerequisites

The name of the top-level directory does not matter. In our examples, we use
"pdfium_build". This directory must not have been used before by `gclient config` as
each directory can only house a single gclient configuration.

```
mkdir pdfium_build
cd pdfium_build/
```

Get the Chromium depot_tools via the instructions. This provides the gclient utility needed below and many other tools needed for PDFium development.

```
git clone https://chromium.googlesource.com/chromium/tools/depot_tools.git
```

Add depot_tools to the front of your PATH (you will probably want to put this in your ~/.bashrc or ~/.zshrc). Assuming you cloned depot_tools to /path/to/pdfium_build/depot_tools:

```
export PATH=/path/to/pdfium_build/depot_tools:$PATH
```

### Get the code
Set the pdfium repository origin.

```
gclient config --unmanaged https://pdfium.googlesource.com/pdfium.git
```

We need to append “.gclient” config file with our target os as Android, to do so
```
echo "target_os = [ 'android' ]" >> .gclient
```

then sync the repository

```
gclient sync
```

Once the sync is completed, run the build dependencies for Android

```
./pdfium/build/install-build-deps-android.sh
```


### Hack the code

Open `./pdfium/BUILD.gn`, scroll to section `config("pdfium_common_config")` (about line 17), change configurations to:

```
 config("pdfium_common_config") {

   cflags = [ "-fvisibility=default" ]
   ldflags = []
   include_dirs = [ "." ]
   defines = [
     "PNG_PREFIX",
     "PNG_USE_READ_MACROS",
     "FPDFSDK_EXPORTS"
   ]
```

1. Add `"-fvisibility=default"` to cflags - Sets visibility of symbols to default. This means other components can reference the symbol, and the symbol definition can be overridden (preempted) by a definition of the same name in another component.
2. Add `“FPDFSDK_EXPORTS”` to defines — This will embed the symbol table in the shared library.


scroll to section `component("pdfium")` (about line 152), append configurations:

```
shared_library("pdfsdk") {
  deps = [":pdfium"]
  ldflags = [ "-latomic" ]
  if (target_os == "android") {
    configs -= [ "//build/config/android:hide_all_but_jni_onload" ]
  }
}
```

Open `./pdfium/build_overrides/build.gni`, scroll to line 25, comment out line 26 ~ line 28:

```
#if (current_cpu == "arm") {
#  arm_use_neon = true
#}
```

Create a build bash:

```
vi ./pdfium/build.sh
```

past these bash command lines to `./pdfium/build.sh`

```bash
#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

set -e

build() {
  local ARCH="$1"
  shift
  local OUT="$DIR/../libpdfsdk/jniLibs/$ARCH/libpdfsdk.so"
  [ -e "$OUT" ] && return
  echo "building $OUT"
  gn gen out --args="target_os=\"android\" pdf_bundle_freetype=true pdf_is_standalone=false is_component_build=false pdf_enable_xfa=false pdf_enable_v8=false is_debug=false is_official_build=true $*"
  ninja -C out pdfsdk
  mkdir -p $(dirname "$OUT") && cp out/libpdfsdk.so "$OUT"
}

build armeabi target_cpu=\"arm\" arm_arch=\"armv5\" arm_version=5 arm_use_thumb=false arm_use_neon=false arm_fpu=\"vfpv2\" arm_float_abi=\"soft\" treat_warnings_as_errors=false
build armeabi-v7a target_cpu=\"arm\" arm_version=7
build arm64-v8a target_cpu=\"arm64\"
build x86 target_cpu=\"x86\"
build x86_64 target_cpu=\"x64\"
#build mipsel target_cpu=\"mipsel\"
#build mips64el target_cpu=\"mips64el\"

cp "$DIR/public/*.h" "$DIR/../libpdfsdk/jniLibs/$ARCH/include/"

```


## Building the code

You can build the pdfium library by running `./build.sh`, after build done all libraries will be located at `../libpdfsdk/jniLibs`.
