/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { McpServer, McpTool } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { exec } from "child_process";
import { promises as fs } from "fs";
import { tmpdir } from "os";
import { join as joinPath } from "path";
import { promisify } from "util";
import { z } from "zod";

const server = new McpServer({
  name: "wear-adb-mcp",
  version: "0.0.1"
});

const SCREENSHOT_TMP_DIR = joinPath(tmpdir(), "gemini-extension-wear-adb");

const GRADLE_PROJECT_ROOT = process.env.GRADLE_PROJECT_ROOT || `../../../`;
const GRADLEW_PATH = process.env.GRADLEW_PATH || "../../../gradlew";
const PACKAGE_NAME = process.env.PACKAGE_NAME || "com.example.wear.tiles";

class CommandError extends Error {
  constructor(
    message: string,
    public command: string,
    public stdout: string,
    public stderr: string
  ) {
    super(message);
  }
}

async function executeCommand(
  command: string,
  options?: { validatePath?: string }
): Promise<string> {
  const execAsync = promisify(exec);
  let cmdResult;
  try {
    cmdResult = await execAsync(command);
  } catch (error: any) {
    throw new CommandError(`Command failed: ${command}`, command, error.stdout, error.stderr);
  }

  if (options?.validatePath) {
    try {
      await fs.access(options.validatePath);
    } catch {
      // The command succeeded, but validation failed.
      throw new CommandError(
        `Command succeeded, but output file was not found at ${options.validatePath}.`,
        command,
        cmdResult.stdout, // stdout from the successful command
        "" // stderr is empty on success
      );
    }
  }

  return cmdResult.stdout;
}

type ToolLogic = (input: any) => Promise<{ isError?: boolean; content: McpTool.Output["content"] }>;

function createTool(
  name: string,
  spec: Omit<McpTool.Spec, "name">,
  logic: ToolLogic
): McpTool.Tool {
  return server.registerTool(name, spec, async (input) => {
    try {
      return await logic(input);
    } catch (error: any) {
      const content: McpTool.Output["content"] = [
        {
          type: "text",
          text: `Error: ${error.message}`
        }
      ];
      if (error instanceof CommandError) {
        content.push(
          {
            type: "text",
            text: `Command: ${error.command}`
          },
          {
            type: "text",
            text: `Output: ${error.stdout}
${error.stderr}`
          }
        );
      }
      return {
        isError: true,
        content
      };
    }
  });
}

createTool(
  "build_apk",
  {
    title: "Builds the debug APK.",
    description:
      "Builds the debug APK from source. Use this to check for compilation errors after a code change. Exact command: `./gradlew :app:assembleDebug`."
  },
  async () => {
    const stdout = await executeCommand(
      `${GRADLEW_PATH} -p ${GRADLE_PROJECT_ROOT} :app:assembleDebug`
    );
    return {
      content: [
        {
          type: "text",
          text: stdout
        }
      ]
    };
  }
);

createTool(
  "install_apk",
  {
    title: "Installs the debug APK.",
    description:
      "Installs the debug APK on the connected device. Exact command: `./gradlew :app:installDebug`."
  },
  async () => {
    const stdout = await executeCommand(
      `${GRADLEW_PATH} -p ${GRADLE_PROJECT_ROOT} :app:installDebug`
    );
    return {
      content: [
        {
          type: "text",
          text: stdout
        }
      ]
    };
  }
);

createTool(
  "add_tile",
  {
    title: "Adds a tile to the carousel.",
    description:
      "Adds a tile to the carousel. If the tile already exists, it is removed and re-added. If the carousel is full, the last tile is removed to make space. Exact command: `adb shell am broadcast -a com.google.android.wearable.app.DEBUG_SURFACE --es operation add-tile --ecn component [COMPONENT_NAME]`.",
    inputSchema: {
      componentName: z
        .string()
        .regex(
          /^[\w.]+\/[\w.]+$/,
          "Invalid component name format. Expected format: com.package.name/com.package.name.service.ClassName"
        )
    }
  },
  async ({ componentName }) => {
    const stdout = await executeCommand(
      `adb shell am broadcast -a com.google.android.wearable.app.DEBUG_SURFACE --es operation add-tile --ecn component ${componentName}`
    );
    return {
      content: [
        {
          type: "text",
          text: stdout
        }
      ]
    };
  }
);

createTool(
  "show_tile",
  {
    title: "Shows a tile.",
    description:
      "Activates and displays the tile at a specific index in the carousel. Exact command: `adb shell am broadcast -a com.google.android.wearable.app.DEBUG_SYSUI --es operation show-tile --ei index [TILE_INDEX]`.",
    inputSchema: { tileIndex: z.number() }
  },
  async ({ tileIndex }) => {
    const stdout = await executeCommand(
      `adb shell am broadcast -a com.google.android.wearable.app.DEBUG_SYSUI --es operation show-tile --ei index ${tileIndex}`
    );
    return {
      content: [
        {
          type: "text",
          text: stdout
        }
      ]
    };
  }
);

createTool(
  "remove_tile",
  {
    title: "Removes a tile.",
    description:
      "Removes all instances of a tile from the carousel. Exact command: `adb shell am broadcast -a com.google.android.wearable.app.DEBUG_SURFACE --es operation remove-tile --ecn component [COMPONENT_NAME]`.",
    inputSchema: {
      componentName: z
        .string()
        .regex(
          /^[\w.]+\/[\w.]+$/,
          "Invalid component name format. Expected format: com.package.name/com.package.name.service.ClassName"
        )
    }
  },
  async ({ componentName }) => {
    const stdout = await executeCommand(
      `adb shell am broadcast -a com.google.android.wearable.app.DEBUG_SURFACE --es operation remove-tile --ecn component ${componentName}`
    );
    return {
      content: [
        {
          type: "text",
          text: stdout
        }
      ]
    };
  }
);

createTool(
  "list_tiles",
  {
    title: "Lists all tiles for the app.",
    description:
      "Lists all available tiles for the current application, returning them in component name format (e.g., com.example.wear.tiles/com.example.wear.tiles.hello.HelloWorldTileService). Exact command: `adb shell cmd package query-services -a androidx.wear.tiles.action.BIND_TILE_PROVIDER --brief | grep [PACKAGE_NAME]`."
  },
  async () => {
    const command = `adb shell cmd package query-services -a androidx.wear.tiles.action.BIND_TILE_PROVIDER --brief | sed 's/^[[:space:]]*//' | grep "${PACKAGE_NAME}" || true | sort`;
    const stdout = await executeCommand(command);
    return {
      content: [
        {
          type: "text",
          text: stdout
        }
      ]
    };
  }
);

async function takeScreenshot(): Promise<string> {
  await fs.mkdir(SCREENSHOT_TMP_DIR, { recursive: true });
  const d = new Date();
  const yyyy = d.getFullYear();
  const MM = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  const hh = String(d.getHours()).padStart(2, "0");
  const mm = String(d.getMinutes()).padStart(2, "0");
  const ss = String(d.getSeconds()).padStart(2, "0");
  const HH = String(Math.floor(d.getMilliseconds() / 10)).padStart(2, "0");
  const timestamp = `${yyyy}${MM}${dd}${hh}${mm}${ss}${HH}`;
  const filename = joinPath(SCREENSHOT_TMP_DIR, `screenshot-${timestamp}.png`);

  const wakeupCommand = "adb exec-out input keyevent KEYCODE_WAKEUP";
  const screencapCommand = `adb exec-out "screencap -p 2>/dev/null"`;

  const magickArgs = [
    "magick -",
    "-alpha set -background none -fill white",
    '\\( +clone -channel A -evaluate set 0 +channel -draw "circle %[fx:w/2],%[fx:h/2] %[fx:w/2],0" \\)',
    "-compose dstin -composite",
    `png:"${filename}"`
  ];
  const magickCommand = magickArgs.join(" ");

  await executeCommand(`${wakeupCommand} && ${screencapCommand} | ${magickCommand}`, {
    validatePath: filename
  });
  return filename;
}

createTool(
  "screenshot_to_stdout",
  {
    title: "Takes a screenshot and returns it as PNG data.",
    description:
      "Takes a screenshot of the connected device and returns the image data as a base64-encoded PNG."
  },
  async () => {
    const filename = await takeScreenshot();
    const fileContent = await fs.readFile(filename);
    const base64Content = fileContent.toString("base64");
    return {
      content: [
        {
          type: "image",
          mimeType: "image/png",
          data: base64Content
        }
      ]
    };
  }
);

createTool(
  "screenshot_to_file",
  {
    title: "Takes a screenshot and saves it to a file.",
    description:
      "Takes a screenshot of the connected device and saves it to a temporary file, returning the absolute path to the PNG file."
  },
  async () => {
    const filename = await takeScreenshot();
    return {
      content: [
        {
          type: "text",
          text: filename
        }
      ]
    };
  }
);

createTool(
  "get_display_size",
  {
    title: "Gets the display size in dp.",
    description:
      "Gets the height and width of the attached display in dp using `adb shell wm size` and `adb shell wm density`"
  },
  async () => {
    const sizeStdout = await executeCommand("adb exec-out wm size");
    const densityStdout = await executeCommand("adb exec-out wm density");

    const sizeMatch = sizeStdout.match(/Physical size: (\d+)x(\d+)/);
    if (!sizeMatch) {
      throw new Error(`Could not parse screen size from: ${sizeStdout}`);
    }
    const widthPx = parseInt(sizeMatch[1], 10);
    const heightPx = parseInt(sizeMatch[2], 10);

    const densityMatch = densityStdout.match(/Physical density: (\d+)/);
    if (!densityMatch) {
      throw new Error(`Could not parse screen density from: ${densityStdout}`);
    }
    const density = parseInt(densityMatch[1], 10);

    if (density === 0) {
      throw new Error("Density cannot be zero.");
    }

    const widthDp = (widthPx * 160) / density;
    const heightDp = (heightPx * 160) / density;

    return {
      content: [
        {
          type: "text",
          text: JSON.stringify({ width: widthDp, height: heightDp })
        }
      ]
    };
  }
);

createTool(
  "get_state",
  {
    title: "Gets the adb state of a connected device.",
    description:
      "Runs `adb get-state` and returns the result. Returns 'error' if the device is not found."
  },
  async () => {
    try {
      const stdout = await executeCommand(`adb get-state`);
      return {
        content: [
          {
            type: "text",
            text: stdout.trim()
          }
        ]
      };
    } catch (e) {
      return {
        content: [
          {
            type: "text",
            text: "error"
          }
        ]
      };
    }
  }
);

createTool(
  "get_serialno",
  {
    title: "Gets the serial number of a connected device.",
    description: "Runs `adb get-serialno` and returns the result."
  },
  async () => {
    const stdout = await executeCommand(`adb get-serialno`);
    return {
      content: [
        {
          type: "text",
          text: stdout.trim()
        }
      ]
    };
  }
);

createTool(
  "debug_info",
  {
    title: "Gets server-side debug info.",
    description: "Returns the values of server-side constants for debugging."
  },
  async () => {
    const serialNo = await executeCommand("adb get-serialno").catch((e) => `Error: ${e.message}`);
    const info = `
CWD: ${process.cwd()}
SCREENSHOT_TMP_DIR: ${SCREENSHOT_TMP_DIR}
GRADLE_PROJECT_ROOT: ${GRADLE_PROJECT_ROOT}
GRADLEW_PATH: ${GRADLEW_PATH}
PACKAGE_NAME: ${PACKAGE_NAME}
ANDROID_SERIAL: ${serialNo.trim()} # adb get-serialno
    `;
    return {
      content: [
        {
          type: "text",
          text: info.trim()
        }
      ]
    };
  }
);

const transport = new StdioServerTransport();

async function main() {
  await server.connect(transport);
}

main().catch(console.error);
