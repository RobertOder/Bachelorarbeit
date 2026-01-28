import asyncio
from fastmcp import Client

async def main():
    async with Client("http://localhost:3333/mcp") as client:
        result = await client.call_tool(
            name="hello", 
            arguments={"name": "Robert"}
        )
    print(result)

asyncio.run(main())