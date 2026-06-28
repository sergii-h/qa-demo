import ITask from "../interfaces/ITask";

const BE_API = import.meta.env.VITE_BE_API || "/v1";

const getTasks = async (baseUrl: string = BE_API): Promise<ITask[]> => {
    const response = await fetch(baseUrl + `/tasks`, { method: 'GET', headers: { 'Content-Type': 'application/json'} });
    return await response.json();
}

const getTask = async (taskId: string, baseUrl: string = BE_API): Promise<ITask> => {
    const response = await fetch(baseUrl + `/tasks/${taskId}`, { method: 'GET', headers: { 'Content-Type': 'application/json'} });

    if (!response.ok) {
        const error = await response.json().catch(() => ({}));
        throw new Error((error as { message?: string }).message || `Request failed (${response.status})`);
    }

    return await response.json();
}

const getIsValid = async (taskId: string, baseUrl: string = BE_API): Promise<boolean> => {
    const response = await fetch(baseUrl + `/tasks/isValid/${taskId}`, { method: 'GET', headers: { 'Content-Type': 'application/json'} });

    if (!response.ok) {
        const error = await response.json().catch(() => ({}));
        throw new Error((error as { message?: string }).message || `Request failed (${response.status})`);
    }

    return await response.json();
}

const createTask = async (task: ITask, baseUrl: string = BE_API): Promise<ITask> => {
    const response = await fetch(baseUrl + `/tasks`, { 
        method: 'POST', 
        body: JSON.stringify({ ...task }), 
        headers: { 'Content-Type': 'application/json'} 
    });
    
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || 'Failed to create task');
    }
    
    return await response.json();
}

const updateTask = async (task: ITask, baseUrl: string = BE_API): Promise<ITask> => {
    const response = await fetch(baseUrl + `/tasks/${task.id}`, { 
        method: 'PUT', 
        body: JSON.stringify({ 
            title: task.title, 
            description: task.description, 
            status: task.status,
            priority: task.priority
        }), 
        headers: { 'Content-Type': 'application/json'} 
    });
    
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || 'Failed to update task');
    }
    
    return await response.json();
}

const deleteTask = async (taskId: string, baseUrl: string = BE_API): Promise<any> => {
    return await fetch(baseUrl + `/tasks/${taskId}`, { method: 'DELETE', headers: { 'Content-Type': 'application/json'} });
}

export  {
    getTasks,
    getTask,
    getIsValid,
    createTask,
    updateTask,
    deleteTask,
    BE_API
}
