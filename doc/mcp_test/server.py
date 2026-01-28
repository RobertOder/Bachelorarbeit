from fastmcp import FastMCP
import httpx
from typing import List, Dict

mcp = FastMCP("Test MCP Server")

############ Tools ############

# Erster Test
@mcp.tool(name="Hello")
def hello(name: str) -> str:
    """welcomes the user."""
    print("Tool \"Hello\" wurde aufgerufen!!")
    return f"Halloechen {name}!"

# Zweiter Test - Woohoooo!!! Geilo laueft super, krasse Welt :D
@mcp.tool(name="list_households")
async def list_households() -> List[Dict]:
    """Returns all households"""
    print("Tool list_households wurde aufgerufen!!")
    async with httpx.AsyncClient(timeout=5.0) as client:
        resp = await client.get("http://localhost:8080/api/household")
        resp.raise_for_status()
        households = resp.json()
    # API-Response formen !!!!!!!!!!!!!!!!!!!!!!!!!!
    return [
        {
            "id": h["id"],
            "name": h["name"]
        }
        for h in households
    ]
    
#Dritter Test
@mcp.tool(name="list_expenditures_from_householdmember")
async def list_expenditures_from_householdmember(id: int) -> List[Dict]:
    """Returns all expenditures from a given householdmember by id"""
    print("Tool list_expenditures_from_householdmember wurde aufgerufen!!")
    async with httpx.AsyncClient(timeout=5.0) as client:
        resp = await client.get(f"http://localhost:8080/api/expenditure?householdMember={id}")
        resp.raise_for_status()
        expenditures = resp.json()
        # API-Response formen !!!!!!!!!!!!!!!!!!!!!!!!!!
        return [
            {
                "id": e["id"],
                "date": e["date"],
                "description": e["description"],
                "amount": e["amount"]
            }
            for e in expenditures
        ]
    
# Vierter Test --> Kann wieder raus aus dem Backend, ich sortiere es direkt hier raus
# @mcp.tool(name="get_expenditures")
# async def get_expenditures_from_householdmember(id: int) -> List[Dict]:
#     """Returns all expenditures from a householdmember by id"""
#     print("Tool get_expenditures_from_householdmember wurde aufgerufen!!")
#     async with httpx.AsyncClient(timeout=5.0) as client:
#         resp = await client.get(f"http://localhost:8080/api/expenditure/withoutReceiptCopies?householdMember=1")
#         resp.raise_for_status()
#         return resp.json()
    
# Fuenfter Test - Usernamen mit einbeziehen
@mcp.tool(name="list_householdmembers")
async def list_householdmember() -> List[Dict]:
    """ Returns all householdmembers"""
    print("Tool list_householdMembers wurde aufgerufen!!")
    async with httpx.AsyncClient(timeout=5.0) as client:
        resp = await client.get(f"http://localhost:8080/api/householdMember")
        resp.raise_for_status()
        users = resp.json()
    # API-Response formen !!!!!!!!!!!!!!!!!!!!!!!!!!
    return [
        {
            "id": u["id"],
            "name": f"{u["firstname"]} {u["name"]}"
        }
        for u in users
    ]

############ Ressources ############

#@mcp.resource("resource://households") - Geht nicht, muss statisches Wissen sein...
#async def get_households() -> List[Dict]:
#    """Returns all households of the backend"""
#    print("Tool get_households wurde aufgerufen!!")
#    async with httpx.AsyncClient(timeout=5.0) as client:
#        resp = await client.get("http://192.168.178.98:8080/api/household")
#        resp.raise_for_status()
#        return resp.json()

############ Prompts ############

if __name__ == "__main__":
    mcp.run(transport="http",
            host="0.0.0.0", 
            port=3333)