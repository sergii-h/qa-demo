let itemResponse = {
    status: 200,
    body: {
        id: '12345',
        name: 'name',
        description: 'description',
        amount: '1',
    },

    set changeStatus(newStatus) {
        this.status = newStatus;
    },

    set changeBody(newBody) {
        this.body = newBody;
    }
}

const mockCreateItem = async (url, data) => {
    if(url.includes('item') && data.method === 'POST') {
        return {
            ok: true,
            status: itemResponse.status,
            json: async () => itemResponse.body,
        };
    }
}

const mockGetItem = async (url, data) => {
    if(url.includes('item/') && data.method === 'GET') {
        return {
            ok: true,
            status: itemResponse.status,
            json: () => itemResponse.body,
        };
    }
}

const mockUpdateItem = async (url, data) => {
    if(url.includes('item') && data.method === 'PUT') {
        return {
            ok: true,
            status: itemResponse.status,
            json: async () => itemResponse.body,
        };
    }
}

export {
    mockCreateItem,
    mockGetItem,
    mockUpdateItem,
    itemResponse
}