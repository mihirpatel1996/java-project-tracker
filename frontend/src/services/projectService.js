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
      // strip projId if present/null before sending
      const { projId, ...payload } = project || {};

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
    try{
      const response = await axios.put(`${API_BASE_URL}/projects/${id}`, project, 
        {
          headers: {
            'Content-Type': 'application/json'
          }
        }
      );
      return response.data;
    }
    catch(error){
      console.log("Error updating project:", error);
      return error;
    }
  },

  // Delete project
  async deleteProject(id) {
    try{
      const response = await axios.delete(`${API_BASE_URL}/projects/${id}`);
      return response.data;

    } 
    catch(error){
      console.log("Error deleting project:"+ error);
      return error;
    }
    // const response = await fetch(`${API_BASE_URL}/projects/${id}`, {
    //   method: 'DELETE',
    // });
    // if (!response.ok) {
    //   throw new Error('Failed to delete project');
    // }
    // return response;
  },
};

export default projectService;
