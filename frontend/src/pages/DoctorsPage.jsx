import { useState, useEffect } from 'react';
import { doctorService, medicalServiceService } from '../services/api';
import { UserCog, Trash2, Edit2, Plus } from 'lucide-react';

export default function DoctorsPage() {
  const [doctors, setDoctors] = useState([]);
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({
    office: '',
    numberOfPTOdays: 21,
    medicalServiceId: ''
  });

  const fetchData = async () => {
    try {
      const [doctorsRes, servicesRes] = await Promise.all([
        doctorService.getAll(),
        medicalServiceService.getAll()
      ]);
      setDoctors(doctorsRes.data);
      setServices(servicesRes.data);
      setLoading(false);
    } catch (error) {
      console.error('Error fetching data:', error);
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const selectedService = services.find(s => s.id === parseInt(formData.medicalServiceId));
      await doctorService.create({
        office: formData.office,
        numberOfPtodays: parseInt(formData.numberOfPTOdays),
        medicalService: selectedService
      });
      setShowForm(false);
      setFormData({ office: '', numberOfPTOdays: 21, medicalServiceId: '' });
      fetchData();
    } catch (error) {
      console.error('Error creating doctor:', error);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Ești sigur că vrei să ștergi acest doctor?')) {
      try {
        await doctorService.delete(id);
        fetchData();
      } catch (error) {
        console.error('Error deleting doctor:', error);
      }
    }
  };

  return (
    <div className="w-full">
      <div className="flex justify-between items-center mb-8">
        <h2 className="text-3xl font-bold text-gray-800">Gestionare Doctori</h2>
        <button 
          onClick={() => setShowForm(!showForm)}
          className="bg-indigo-600 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-indigo-700"
        >
          <Plus size={20} />
          Adaugă Doctor
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleSubmit} className="bg-white p-6 rounded-xl shadow-md mb-8 grid grid-cols-2 gap-4">
          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold text-gray-600">Cabinet / Oficiu</label>
            <input 
              type="text" 
              required
              className="border p-2 rounded-md"
              value={formData.office}
              onChange={(e) => setFormData({...formData, office: e.target.value})}
              placeholder="ex: Cabinet 204"
            />
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold text-gray-600">Zile Concediu (PTO)</label>
            <input 
              type="number" 
              required
              className="border p-2 rounded-md"
              value={formData.numberOfPTOdays}
              onChange={(e) => setFormData({...formData, numberOfPTOdays: e.target.value})}
            />
          </div>
          <div className="flex flex-col gap-1 col-span-2">
            <label className="text-sm font-semibold text-gray-600">Serviciu Medical / Specializare</label>
            <select 
              required
              className="border p-2 rounded-md"
              value={formData.medicalServiceId}
              onChange={(e) => setFormData({...formData, medicalServiceId: e.target.value})}
            >
              <option value="">Selectează un serviciu...</option>
              {services.map(service => (
                <option key={service.id} value={service.id}>
                  {service.name} ({service.specialization})
                </option>
              ))}
            </select>
          </div>
          <div className="col-span-2 flex justify-end gap-3 mt-2">
            <button 
              type="button" 
              onClick={() => setShowForm(false)}
              className="text-gray-600 px-4 py-2 hover:bg-gray-100 rounded-lg"
            >
              Anulează
            </button>
            <button 
              type="submit" 
              className="bg-indigo-600 text-white px-6 py-2 rounded-lg hover:bg-indigo-700"
            >
              Salvează
            </button>
          </div>
        </form>
      )}

      {loading ? (
        <div className="text-center py-10">Se încarcă...</div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {doctors.map((doctor) => (
            <div key={doctor.id} className="bg-white p-6 rounded-xl shadow-md border-l-4 border-indigo-500 hover:shadow-lg transition-shadow">
              <div className="flex justify-between items-start">
                <div>
                  <h3 className="text-xl font-bold text-gray-900">Dr. {doctor.id}</h3>
                  <p className="text-indigo-600 font-medium">{doctor.medicalService?.name || 'Fără serviciu'}</p>
                </div>
                <div className="flex gap-2">
                  <button className="p-2 text-gray-400 hover:text-indigo-600"><Edit2 size={18} /></button>
                  <button 
                    onClick={() => handleDelete(doctor.id)}
                    className="p-2 text-gray-400 hover:text-red-600"
                  >
                    <Trash2 size={18} />
                  </button>
                </div>
              </div>
              
              <div className="mt-4 space-y-2 text-sm text-gray-600">
                <div className="flex justify-between">
                  <span>Cabinet:</span>
                  <span className="font-semibold text-gray-800">{doctor.office}</span>
                </div>
                <div className="flex justify-between">
                  <span>Zile PTO rămase:</span>
                  <span className="font-semibold text-gray-800">{doctor.numberOfPtodays}</span>
                </div>
                <div className="flex justify-between">
                  <span>Specializare:</span>
                  <span className="font-semibold text-gray-800">{doctor.medicalService?.specialization}</span>
                </div>
              </div>
            </div>
          ))}
          {doctors.length === 0 && (
            <div className="col-span-2 text-center py-10 text-gray-500">Nu există doctori înregistrați.</div>
          )}
        </div>
      )}
    </div>
  );
}

