import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import HomePage from './pages/HomePage';
import CreateProjectPage from './pages/CreateProjectPage';
import LoginPage from './pages/LoginPage';


function App() {
  return (
    // <Router>
    //   <div className="min-h-screen bg-gray-100">
    //     <Routes>
    //       <Route path="/login" element={<LoginPage />}></Route>
    //       <Route path="/" element={<HomePage />} />
    //       <Route path="/project/new" element={<CreateProjectPage/>} />
    //     </Routes>
    //   </div>
    // </Router>
    <AuthProvider>
      <Router>
        <div className="min-h-screen bg-gray-100">
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route
              path="/"
              element={
                <ProtectedRoute>
                  <HomePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/project/new"
              element={
                <ProtectedRoute>
                  <CreateProjectPage />
                </ProtectedRoute>
              }
            />
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;