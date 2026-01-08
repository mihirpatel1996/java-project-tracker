import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import projectService from '../services/projectService';

function CreateProjectPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const [formData, setFormData] = useState({
    projName: '',
    clientCompany: '',
    clientEmail: '',
    projType: '',
    projTitle: '',
    currPhase: '',
    status: 'Active',
    projDetails: '',
    startDate: '',
    estCompDate: '',
  });

  // Auto-populate clientCompany and clientEmail from user data
  useEffect(() => {
    if (user) {
      setFormData(prev => ({
        ...prev,
        clientCompany: user.companyName || '',
        clientEmail: user.email || '',
      }));
    }
  }, [user]);

  const projectTypes = [
    'Drug Discovery',
    'Clinical Trial',
    'Manufacturing',
    'Research & Development',
    'Regulatory Affairs',
    'Quality Assurance',
  ];

  const phases = [
    'Preclinical',
    'Phase I',
    'Phase II',
    'Phase III',
    'Approval',
  ];

  const statuses = ['Active', 'On Hold', 'Completed', 'Cancelled'];

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      // Format dates for backend (LocalDateTime format)
      const payload = {
        ...formData,
        startDate: formData.startDate ? `${formData.startDate}T00:00:00` : null,
        estCompDate: formData.estCompDate ? `${formData.estCompDate}T00:00:00` : null,
        createdDate: new Date().toISOString(),
        updatedDate: new Date().toISOString(),
      };

      await projectService.createProject(payload);
      navigate('/');
    } catch (err) {
      setError('Failed to create project. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container mx-auto px-4 py-8 max-w-3xl">
      {/* Header */}
      <div className="mb-8">
        <Link
          to="/"
          className="text-blue-600 hover:text-blue-800 flex items-center gap-2 mb-4"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="h-5 w-5"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fillRule="evenodd"
              d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z"
              clipRule="evenodd"
            />
          </svg>
          Back to Projects
        </Link>
        <h1 className="text-3xl font-bold text-gray-900">Create New Project</h1>
        <p className="text-gray-600 mt-1">Fill in the details to create a new project</p>
      </div>

      {/* Error Message */}
      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded-lg mb-6">
          {error}
        </div>
      )}

      {/* Form */}
      <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow-md p-6">
        {/* Email Notifications Info */}
        <div className="mb-6 p-4 bg-blue-50 border border-blue-200 rounded-lg flex items-start gap-3">
          <svg className="h-5 w-5 text-blue-500 mt-0.5 flex-shrink-0" viewBox="0 0 20 20" fill="currentColor">
            <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
          </svg>
          <div>
            <p className="text-blue-800 text-sm font-medium">Email Notifications Enabled</p>
            <p className="text-blue-600 text-sm">You'll receive email updates when the project status changes. You can disable this in the project settings later.</p>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Project Name */}
          <div className="col-span-2 md:col-span-1">
            <label htmlFor="projName" className="block text-sm font-medium text-gray-700 mb-1">
              Project Name <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              id="projName"
              name="projName"
              value={formData.projName}
              onChange={handleChange}
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Enter project name"
            />
          </div>

          {/* Project Title */}
          <div className="col-span-2 md:col-span-1">
            <label htmlFor="projTitle" className="block text-sm font-medium text-gray-700 mb-1">
              Project Title <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              id="projTitle"
              name="projTitle"
              value={formData.projTitle}
              onChange={handleChange}
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Enter project title"
            />
          </div>

          {/* Client Company - Read Only */}
          <div className="col-span-2 md:col-span-1">
            <label htmlFor="clientCompany" className="block text-sm font-medium text-gray-700 mb-1">
              Client Company
              <span className="text-xs text-gray-400 ml-1">(auto-assigned from your profile)</span>
            </label>
            <input
              type="text"
              id="clientCompany"
              name="clientCompany"
              value={formData.clientCompany}
              disabled
              className="w-full px-3 py-2 border border-gray-200 rounded-lg bg-gray-50 text-gray-500 cursor-not-allowed"
            />
          </div>

          {/* Client Email - Read Only */}
          <div className="col-span-2 md:col-span-1">
            <label htmlFor="clientEmail" className="block text-sm font-medium text-gray-700 mb-1">
              Client Email
              <span className="text-xs text-gray-400 ml-1">(auto-assigned from your profile)</span>
            </label>
            <input
              type="email"
              id="clientEmail"
              name="clientEmail"
              value={formData.clientEmail}
              disabled
              className="w-full px-3 py-2 border border-gray-200 rounded-lg bg-gray-50 text-gray-500 cursor-not-allowed"
            />
          </div>

          {/* Project Type */}
          <div className="col-span-2 md:col-span-1">
            <label htmlFor="projType" className="block text-sm font-medium text-gray-700 mb-1">
              Project Type <span className="text-red-500">*</span>
            </label>
            <select
              id="projType"
              name="projType"
              value={formData.projType}
              onChange={handleChange}
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="">Select project type</option>
              {projectTypes.map((type) => (
                <option key={type} value={type}>
                  {type}
                </option>
              ))}
            </select>
          </div>

          {/* Current Phase */}
          <div className="col-span-2 md:col-span-1">
            <label htmlFor="currPhase" className="block text-sm font-medium text-gray-700 mb-1">
              Current Phase <span className="text-red-500">*</span>
            </label>
            <select
              id="currPhase"
              name="currPhase"
              value={formData.currPhase}
              onChange={handleChange}
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="">Select phase</option>
              {phases.map((phase) => (
                <option key={phase} value={phase}>
                  {phase}
                </option>
              ))}
            </select>
          </div>

          {/* Status */}
          <div className="col-span-2 md:col-span-1">
            <label htmlFor="status" className="block text-sm font-medium text-gray-700 mb-1">
              Status <span className="text-red-500">*</span>
            </label>
            <select
              id="status"
              name="status"
              value={formData.status}
              onChange={handleChange}
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              {statuses.map((status) => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </select>
          </div>

          {/* Start Date */}
          <div className="col-span-2 md:col-span-1">
            <label htmlFor="startDate" className="block text-sm font-medium text-gray-700 mb-1">
              Start Date
            </label>
            <input
              type="date"
              id="startDate"
              name="startDate"
              value={formData.startDate}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          {/* Estimated Completion Date */}
          <div className="col-span-2 md:col-span-1">
            <label htmlFor="estCompDate" className="block text-sm font-medium text-gray-700 mb-1">
              Est. Completion Date
            </label>
            <input
              type="date"
              id="estCompDate"
              name="estCompDate"
              value={formData.estCompDate}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          {/* Project Details */}
          <div className="col-span-2">
            <label htmlFor="projDetails" className="block text-sm font-medium text-gray-700 mb-1">
              Project Details
            </label>
            <textarea
              id="projDetails"
              name="projDetails"
              value={formData.projDetails}
              onChange={handleChange}
              rows={4}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Enter project details..."
            />
          </div>
        </div>

        {/* Submit Button */}
        <div className="mt-6 flex justify-end gap-4">
          <Link
            to="/"
            className="px-4 py-2 text-gray-700 bg-gray-200 hover:bg-gray-300 rounded-lg transition duration-200"
          >
            Cancel
          </Link>
          <button
            type="submit"
            disabled={loading}
            className="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition duration-200 flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? (
              <>
                <svg
                  className="animate-spin h-5 w-5 text-white"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                >
                  <circle
                    className="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    strokeWidth="4"
                  ></circle>
                  <path
                    className="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  ></path>
                </svg>
                Creating...
              </>
            ) : (
              <>
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-5 w-5"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path
                    fillRule="evenodd"
                    d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z"
                    clipRule="evenodd"
                  />
                </svg>
                Create Project
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
}

export default CreateProjectPage;