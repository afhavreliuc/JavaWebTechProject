import { useState, useEffect } from 'react';
import { patientService, insuranceProviderService, subscriptionPlanService } from '../services/api';
import { UserPlus, Trash2, Edit2, CheckCircle2, AlertCircle, Loader2 } from 'lucide-react';

export default function PatientsPage() {
  const [patients, setPatients] = useState([]);
  const [insuranceProviders, setInsuranceProviders] = useState([]);
  const [subscriptionPlans, setSubscriptionPlans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editId, setEditId] = useState(null);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    age: '',
    medicalRecord: '',
    insuranceProviderId: '',
    subscriptionPlanId: '',
    subscription: false,
    sex: true
  });

  const fetchPatients = async () => {
    try {
      const response = await patientService.getAll();
      setPatients(response.data);
    } catch (error) {
      console.error('Error fetching patients:', error);
    }
  };

  const fetchInsuranceProviders = async () => {
    try {
      const response = await insuranceProviderService.getAll();
      setInsuranceProviders(response.data);
    } catch (error) {
      console.error('Error fetching insurance providers:', error);
    }
  };

  const fetchSubscriptionPlans = async () => {
    try {
      const response = await subscriptionPlanService.getAll();
      setSubscriptionPlans(response.data);
    } catch (error) {
      console.error('Error fetching subscription plans:', error);
    }
  };

  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      await Promise.all([
        fetchPatients(), 
        fetchInsuranceProviders(),
        fetchSubscriptionPlans()
      ]);
      setLoading(false);
    };
    loadData();
  }, []);

  const handleEdit = (patient) => {
    setEditId(patient.id);
    setFormData({
      name: patient.name,
      age: patient.age.toString(),
      medicalRecord: patient.medicalRecord || '',
      insuranceProviderId: patient.insuranceProvider?.id || '',
      subscriptionPlanId: patient.activeSubscription?.id || '',
      subscription: patient.subscription,
      sex: patient.sex
    });
    setShowForm(true);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setMessage(null);
    try {
      const payload = {
        name: formData.name,
        age: parseInt(formData.age),
        medicalRecord: formData.medicalRecord,
        insuranceProvider: formData.insuranceProviderId 
          ? { id: parseInt(formData.insuranceProviderId) } 
          : null,
        activeSubscription: formData.subscriptionPlanId
          ? { id: parseInt(formData.subscriptionPlanId) }
          : null,
        subscription: formData.subscription || !!formData.subscriptionPlanId,
        sex: formData.sex
      };

      if (editId) {
        await patientService.update(editId, payload);
        setMessage({ type: 'success', text: 'Pacient actualizat cu succes!' });
      } else {
        await patientService.create(payload);
        setMessage({ type: 'success', text: 'Pacient salvat cu succes!' });
      }
      
      setShowForm(false);
      setEditId(null);
      setFormData({ name: '', age: '', medicalRecord: '', insuranceProviderId: '', subscriptionPlanId: '', subscription: false, sex: true });
      fetchPatients();
    } catch (error) {
      console.error('Error saving patient:', error);
      setMessage({ 
        type: 'error', 
        text: error.response?.data?.message || 'Eroare la salvarea pacientului. Verificați dacă serverul este pornit.' 
      });
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Ești sigur că vrei să ștergi acest pacient?')) {
      try {
        await patientService.delete(id);
        fetchPatients();
      } catch (error) {
        console.error('Error deleting patient:', error);
      }
    }
  };

  return (
    <div className="w-full">
      <div className="flex justify-between items-center mb-8">
        <h2 className="text-3xl font-bold text-gray-800">Gestionare Pacienți</h2>
        <button 
          onClick={() => {
            if (showForm && editId) {
              setEditId(null);
              setFormData({ name: '', age: '', medicalRecord: '', insuranceProviderId: '', subscription: false, sex: true });
            } else {
              setShowForm(!showForm);
            }
          }}
          className="bg-indigo-600 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-indigo-700"
        >
          <UserPlus size={20} />
          {editId ? 'Mod Nou Pacient' : 'Adaugă Pacient'}
        </button>
      </div>

      {message && (
        <div className={`mb-6 p-4 rounded-lg flex items-center gap-3 animate-in fade-in duration-300 ${
          message.type === 'success' ? 'bg-green-50 text-green-700 border border-green-200' : 'bg-red-50 text-red-700 border border-red-200'
        }`}>
          {message.type === 'success' ? <CheckCircle2 size={20} /> : <AlertCircle size={20} />}
          <p className="font-medium">{message.text}</p>
        </div>
      )}

      {showForm && (
        <form onSubmit={handleSubmit} className="bg-white p-6 rounded-xl shadow-md mb-8 grid grid-cols-2 gap-4 border border-indigo-100 ring-2 ring-indigo-500/10">
          <h3 className="col-span-2 text-lg font-bold text-indigo-900 mb-2 border-b pb-2">
            {editId ? `Editare Pacient #${editId}` : 'Adăugare Pacient Nou'}
          </h3>
          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold text-gray-600">
              Nume Complet <span className="text-blue-600">*</span>
            </label>
            <input 
              type="text" 
              required
              disabled={saving}
              className="border p-2 rounded-md focus:ring-2 focus:ring-indigo-500 outline-none"
              value={formData.name}
              onChange={(e) => setFormData({...formData, name: e.target.value})}
            />
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold text-gray-600">
              Vârstă <span className="text-blue-600">*</span>
            </label>
            <input 
              type="number" 
              required
              disabled={saving}
              className="border p-2 rounded-md focus:ring-2 focus:ring-indigo-500 outline-none"
              value={formData.age}
              onChange={(e) => setFormData({...formData, age: e.target.value})}
            />
          </div>
          <div className="flex flex-col gap-1 col-span-2">
            <label className="text-sm font-semibold text-gray-600">Istoric Medical</label>
            <textarea 
              disabled={saving}
              className="border p-2 rounded-md h-24 focus:ring-2 focus:ring-indigo-500 outline-none"
              value={formData.medicalRecord}
              onChange={(e) => setFormData({...formData, medicalRecord: e.target.value})}
            />
          </div>
          <div className="flex items-center gap-4 col-span-2">
            <div className="flex flex-col gap-1">
              <label className="text-sm font-semibold text-gray-600">Asigurător</label>
              <select
                disabled={saving}
                className="border p-2 rounded-md focus:ring-2 focus:ring-indigo-500 outline-none min-w-[200px]"
                value={formData.insuranceProviderId}
                onChange={(e) => setFormData({...formData, insuranceProviderId: e.target.value})}
              >
                <option value="">Fără asigurare (-)</option>
                {insuranceProviders.map(provider => (
                  <option key={provider.id} value={provider.id}>
                    {provider.name}
                  </option>
                ))}
              </select>
            </div>

            <div className="flex flex-col gap-1">
              <label className="text-sm font-semibold text-gray-600">Abonament (Plan)</label>
              <select
                disabled={saving}
                className="border p-2 rounded-md focus:ring-2 focus:ring-indigo-500 outline-none min-w-[200px]"
                value={formData.subscriptionPlanId}
                onChange={(e) => setFormData({...formData, subscriptionPlanId: e.target.value})}
              >
                <option value="">Fără abonament (-)</option>
                {subscriptionPlans.map(plan => (
                  <option key={plan.id} value={plan.id}>
                    {plan.name} ({plan.monthlyFee} RON)
                  </option>
                ))}
              </select>
            </div>

            <div className="flex flex-col gap-1">
              <label className="text-sm font-semibold text-gray-600">Sex</label>
              <select 
                disabled={saving}
                className="border p-2 rounded-md focus:ring-2 focus:ring-indigo-500 outline-none"
                value={formData.sex ? 'true' : 'false'}
                onChange={(e) => setFormData({...formData, sex: e.target.value === 'true'})}
              >
                <option value="true">Masculin</option>
                <option value="false">Feminin</option>
              </select>
            </div>
          </div>
          <div className="col-span-2 flex justify-end gap-3 mt-2">
            <button 
              type="button" 
              onClick={() => setShowForm(false)}
              disabled={saving}
              className="text-gray-600 px-4 py-2 hover:bg-gray-100 rounded-lg transition-colors"
            >
              Anulează
            </button>
            <button 
              type="submit" 
              disabled={saving}
              className="bg-indigo-600 text-white px-6 py-2 rounded-lg hover:bg-indigo-700 transition-all flex items-center gap-2 disabled:opacity-70 disabled:cursor-not-allowed shadow-md"
            >
              {saving ? (
                <>
                  <Loader2 className="animate-spin" size={18} />
                  Se salvează...
                </>
              ) : 'Salvează'}
            </button>
          </div>
        </form>
      )}

      {loading ? (
        <div className="text-center py-10">Se încarcă...</div>
      ) : (
        <div className="bg-white rounded-xl shadow-md overflow-hidden">
          <table className="w-full text-left">
            <thead className="bg-gray-50 border-b">
              <tr>
                <th className="px-6 py-4 font-semibold text-gray-700">ID</th>
                <th className="px-6 py-4 font-semibold text-gray-700">Nume</th>
                <th className="px-6 py-4 font-semibold text-gray-700">Vârstă</th>
                <th className="px-6 py-4 font-semibold text-gray-700">Sex</th>
                <th className="px-6 py-4 font-semibold text-gray-700">Abonament</th>
                <th className="px-6 py-4 font-semibold text-gray-700">Asigurător</th>
                <th className="px-6 py-4 font-semibold text-gray-700">Acțiuni</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {patients.map((patient) => (
                <tr key={patient.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 text-gray-600">#{patient.id}</td>
                  <td className="px-6 py-4 font-medium text-gray-900">{patient.name}</td>
                  <td className="px-6 py-4 text-gray-600">{patient.age} ani</td>
                  <td className="px-6 py-4 text-gray-600">{patient.sex ? 'M' : 'F'}</td>
                  <td className="px-6 py-4">
                    <span className={`px-3 py-1 rounded-full text-xs font-medium ${patient.activeSubscription || patient.subscription ? 'bg-indigo-100 text-indigo-700' : 'bg-gray-100 text-gray-700'}`}>
                      {patient.activeSubscription ? patient.activeSubscription.name : (patient.subscription ? 'Da' : 'Nu')}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-gray-600">
                    {patient.insuranceProvider ? patient.insuranceProvider.name : '-'}
                  </td>
                  <td className="px-6 py-4 flex gap-3">
                    <button 
                      onClick={() => handleEdit(patient)}
                      className="text-indigo-600 hover:text-indigo-900"
                    >
                      <Edit2 size={18} />
                    </button>
                    <button 
                      onClick={() => handleDelete(patient.id)}
                      className="text-red-600 hover:text-red-900"
                    >
                      <Trash2 size={18} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {patients.length === 0 && (
            <div className="text-center py-10 text-gray-500">Nu există pacienți înregistrați.</div>
          )}
        </div>
      )}
    </div>
  );
}

