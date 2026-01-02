import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';

export const projectService = {
  // Get all projects
  async getAllProjects() {
    try {
      const response = await axios.get(`${API_BASE_URL}/projects`);
      return response.data;
    } catch (error) {
      console.error('Error fetching projects:', error);
      throw error;
    }

    // const response = await fetch(`${API_BASE_URL}/projects`);
    // if (!response.ok) {
    //   throw new Error('Failed to fetch projects');
    // }
    // console.log("getAllProjects called");
    // return response.json();
  },

  // Get single project by ID
  async getProjectById(id) {
    try{
      const response = await axios.get(`{API_BASE_URL}/projects/${id}`);
      return response.data;
    }
    catch(error){
      console.error('Error fetching project:', error);
      throw error;
    }
  },

  // Create new project
  async createProject(project) {
    try{
      console.log("creating project: "+project);
      console.log("project type:"+typeof(project));
      // strip projId if present/null before sending
      const { projId, ...payload } = project || {};

      // DEBUG: log exact JSON sent to server
      console.log('createProject payload JSON:', JSON.stringify(payload));
      const response = await axios.post(`${API_BASE_URL}/projects`, payload,
        {
          headers: {
            'Content-Type': 'application/json',
          },
        }
      );
      
      return response.data;
    }
    catch(error){
      console.log('Error creating project:', error);
      throw error;
    }
  },

  // Update existing project
  async updateProject(id, project) {
    const response = await fetch(`${API_BASE_URL}/project/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(project),
    });
    if (!response.ok) {
      throw new Error('Failed to update project');
    }
    return response;
  },

  // Delete project
  async deleteProject(id) {
    const response = await fetch(`${API_BASE_URL}/projects/${id}`, {
      method: 'DELETE',
    });
    if (!response.ok) {
      throw new Error('Failed to delete project');
    }
    return response;
  },
};

export default projectService;
