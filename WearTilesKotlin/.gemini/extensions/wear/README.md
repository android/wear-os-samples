# Gemini Extension Server for Wear OS Tiles Development

This server provides a set of tools via the Model-Context Protocol (MCP) to assist with Wear OS
Tiles development. It allows an AI agent, like Gemini in Android Studio, to interact with your
development environment to perform tasks such as building, installing, and managing Wear OS Tiles.

## Prerequisites

- [Node.js](https://nodejs.org/) (version 18 or higher)

## Installation

1.  Install the necessary dependencies from the directory containing `package.json`:

    ```sh
    npm install
    ```

## Usage

This server is designed to be run by an MCP client, such as the
[Gemini CLI](https://github.com/google-gemini/gemini-cli) or
[Gemini in Android Studio](https://github.com/google-gemini/gemini-cli).

### With Gemini CLI

This extension will automatically be loaded if `gemini` is run from the `WearTilesKotlin` directory
(i.e. the directory containing the `.gemini` directory). More info on
[Gemini CLI Extensions](https://github.com/google-gemini/gemini-cli/blob/main/docs/extension.md).

To verify Gemini CLI has access to the extension, run `gemini -v`:

```sh
$ gemini -v
Loading extension: wear (version: 0.0.1)
0.1.9
```

(The extension should also appear when entering the prompt `/mcp`, or hitting ctrl+t.)

### With Android Studio

For integration with Android Studio, please refer to the official documentation,
[Add an MCP server](https://developer.android.com/studio/preview/gemini/agent-mode#add-mcp). If
you're having trouble, try hard-coding all relevant pathnames in `mcp.json`, and provide a suitable
`PATH` environment variable. If possible, check that the Gemini CLI approach works first.

## Available Tools

This server provides the following tools:

- **`build_apk`**: Builds the debug APK from source.
- **`install_apk`**: Installs the debug APK on the connected device.
- **`add_tile`**: Adds a tile to the carousel.
- **`show_tile`**: Activates and displays the tile at a specific index in the carousel.
- **`remove_tile`**: Removes all instances of a tile from the carousel.
- **`list_tiles`**: Lists all available tiles for the current application, returning them in
  component name format.
- **`screenshot_to_stdout`**: Takes a screenshot of the connected device and returns the image data
  as a base64-encoded PNG.
- **`screenshot_to_file`**: Takes a screenshot of the connected device and saves it to a temporary
  file, returning the absolute path to the PNG file.
- **`get_display_size`**: Gets the height and width of the attached display in dp.
- **`get_state`**: Returns the state of the connected device.
- **`get_serialno`**: Returns the serial number of the connected device.
- **`debug_info`**: Returns the values of server-side constants for debugging.

## Development

### Inspecting the Server

You can inspect the tools and capabilities provided by the server using the MCP Inspector. This is
useful for debugging or understanding the server's functionality without connecting a full agent.
See <https://github.com/modelcontextprotocol/inspector> for more information.

```sh
DANGEROUSLY_OMIT_AUTH=true npx -y @modelcontextprotocol/inspector npx tsx server.ts
```

### Updating Dependencies

To keep the project's dependencies up to date:

1.  **Check for outdated packages:**

    ```sh
    npx npm-check-updates
    ```

2.  **Update `package.json` with the latest versions:**

    ```sh
    npx npm-check-updates -u
    ```

    After updating, run `npm install` to install the new package versions.
