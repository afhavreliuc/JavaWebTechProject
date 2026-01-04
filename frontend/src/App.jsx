import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import { LayoutDashboard, Users, UserCog, CalendarDays, Database } from 'lucide-react';
import PatientsPage from './pages/PatientsPage';
import DoctorsPage from './pages/DoctorsPage';
import AppointmentsPage from './pages/AppointmentsPage';
import DataWarehousePage from './pages/DataWarehousePage';

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-gray-50 flex">
        {/* Sidebar */}
        <aside className="w-64 bg-indigo-700 text-white shadow-xl">
          <div className="p-6">
            <h1 className="text-2xl font-bold flex items-center gap-2">
              <LayoutDashboard size={28} />
              MedManager
            </h1>
          </div>
          <nav className="mt-6 px-4">
            <Link to="/patients" className="flex items-center gap-3 py-3 px-4 rounded-lg hover:bg-indigo-600 transition-colors">
              <Users size={20} />
              Pacienți
            </Link>
            <Link to="/doctors" className="flex items-center gap-3 py-3 px-4 rounded-lg hover:bg-indigo-600 transition-colors">
              <UserCog size={20} />
              Doctori
            </Link>
            <Link to="/appointments" className="flex items-center gap-3 py-3 px-4 rounded-lg hover:bg-indigo-600 transition-colors">
              <CalendarDays size={20} />
              Programări
            </Link>
            <div className="my-4 border-t border-indigo-500 opacity-50"></div>
            <Link to="/dw" className="flex items-center gap-3 py-3 px-4 rounded-lg bg-indigo-800 hover:bg-indigo-900 transition-colors">
              <Database size={20} />
              Data Warehouse
            </Link>
          </nav>
        </aside>

        {/* Main Content */}
        <main className="flex-1 p-8">
          <Routes>
            <Route path="/patients" element={<PatientsPage />} />
            <Route path="/doctors" element={<DoctorsPage />} />
            <Route path="/appointments" element={<AppointmentsPage />} />
            <Route path="/dw" element={<DataWarehousePage />} />
            <Route path="/" element={
              <div className="text-center mt-20">
                <h2 className="text-4xl font-bold text-gray-800">Bine ai venit la MedManager</h2>
                <p className="text-gray-600 mt-4">Gestionează pacienții, doctorii și programările tale într-un singur loc.</p>
              </div>
            } />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;

