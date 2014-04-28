package com.RHCCServer.Model;
import java.util.ArrayList;

// this class represents a group and its methods and properties
public class Group 
{
	private String groupName;
	private ArrayList<Client> clients;
	
	public Group(String name)
	{
		groupName = name;
		clients = new ArrayList<Client>();
	}
	
	public void addMember(Client client)
	{
		clients.add(client);
	}
	
	public boolean removeMember(Client client)
	{
		return clients.remove(client);
	}
	
	public ArrayList<Client> getAllMembers()
	{
		return this.clients;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
}